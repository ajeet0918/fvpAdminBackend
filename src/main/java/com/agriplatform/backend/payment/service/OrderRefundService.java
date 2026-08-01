package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.order.model.OrderPaymentStatus;
import com.agriplatform.backend.order.model.OrderStatusHistory;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.repository.PurchaseOrderRepository;
import com.agriplatform.backend.payment.dto.CashfreeRefundResult;
import com.agriplatform.backend.payment.dto.CashfreeRefundSnapshot;
import com.agriplatform.backend.payment.dto.CreateOrderRefundRequest;
import com.agriplatform.backend.payment.dto.OrderRefundResponse;
import com.agriplatform.backend.payment.dto.OrderRefundSummaryResponse;
import com.agriplatform.backend.payment.dto.RefundWebhookUpdate;
import com.agriplatform.backend.payment.model.OrderRefund;
import com.agriplatform.backend.payment.model.OrderRefundStatus;
import com.agriplatform.backend.payment.repository.OrderRefundRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderRefundService {
    private static final String DEFAULT_REFUND_NOTE = "Order refund approved by administrator.";
    private static final String DEFAULT_REFUND_SPEED = "STANDARD";
    private static final String WEBHOOK_ACTOR = "CASHFREE_WEBHOOK";
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final java.util.Set<String> REFUND_SPEEDS = java.util.Set.of("STANDARD", "INSTANT");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderRefundRepository orderRefundRepository;
    private final CashfreeGatewayService cashfreeGatewayService;

    public OrderRefundService(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderRefundRepository orderRefundRepository,
            CashfreeGatewayService cashfreeGatewayService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderRefundRepository = orderRefundRepository;
        this.cashfreeGatewayService = cashfreeGatewayService;
    }

    @Transactional
    public OrderRefundResponse createRefund(Long orderId, CreateOrderRefundRequest request, String requestedBy) {
        PurchaseOrder order = purchaseOrderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        validateRefundRequest(order, request.amount());

        String refundId = buildRefundId(order.getId());
        String speed = normalizeSpeed(request.speed());
        String note = normalizeNote(request.note());
        OrderRefund refund = new OrderRefund(
                order,
                refundId,
                request.amount(),
                order.getCurrency(),
                speed,
                note,
                requestedBy
        );
        order.addRefund(refund);
        purchaseOrderRepository.saveAndFlush(order);

        applyGatewayResult(refund, order, request.amount(), refundId, note, speed);
        return toResponse(orderRefundRepository.save(refund));
    }

    @Transactional
    public void reconcileWebhook(RefundWebhookUpdate update) {
        validateWebhookUpdate(update);
        PurchaseOrder order = purchaseOrderRepository.findByOrderNumberForUpdate(update.orderNumber())
                .orElseThrow(() -> new IllegalArgumentException("Order payment reference not found"));
        applyUpdate(order, update);
    }

    @Transactional
    public List<OrderRefundResponse> syncRefunds(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        List<CashfreeRefundSnapshot> snapshots = cashfreeGatewayService.fetchRefunds(order);
        snapshots.stream()
                .map(snapshot -> toWebhookUpdate(order.getOrderNumber(), snapshot))
                .forEach(update -> applyUpdate(order, update));
        return toResponses(order);
    }

    private void applyUpdate(PurchaseOrder order, RefundWebhookUpdate update) {
        OrderRefund refund = findRefund(update);
        boolean newRefund = refund == null;
        if (refund == null) {
            refund = createWebhookRefund(order, update);
            order.addRefund(refund);
        }
        boolean statusChanged = refund.applyGatewayStatus(
                update.providerRefundId(),
                update.providerPaymentId(),
                update.status(),
                update.statusDescription(),
                update.refundArn(),
                update.processedAt()
        );
        orderRefundRepository.save(refund);
        if (newRefund || statusChanged) {
            addRefundHistory(order, refund);
        }
        purchaseOrderRepository.save(order);
    }

    private RefundWebhookUpdate toWebhookUpdate(String orderNumber, CashfreeRefundSnapshot snapshot) {
        return new RefundWebhookUpdate(
                orderNumber,
                snapshot.refundId(),
                snapshot.providerRefundId(),
                snapshot.providerPaymentId(),
                snapshot.amount(),
                snapshot.currency(),
                snapshot.status(),
                snapshot.speed(),
                snapshot.note(),
                snapshot.statusDescription(),
                snapshot.refundArn(),
                snapshot.processedAt()
        );
    }

    public OrderRefundSummaryResponse summarize(PurchaseOrder order) {
        List<OrderRefund> refunds = order.getRefunds();
        BigDecimal paidAmount = resolvePaidAmount(order);
        BigDecimal refundedAmount = sumByStatus(refunds, OrderRefundStatus.SUCCESS);
        BigDecimal pendingAmount = refunds.stream()
                .filter(this::reservesRefundAmount)
                .map(OrderRefund::getAmount)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal refundableAmount = paidAmount.subtract(refundedAmount).subtract(pendingAmount).max(ZERO);
        return new OrderRefundSummaryResponse(
                resolveSummaryStatus(refunds, paidAmount, refundedAmount),
                refundedAmount,
                pendingAmount,
                refundableAmount
        );
    }

    public List<OrderRefundResponse> toResponses(PurchaseOrder order) {
        return order.getRefunds().stream()
                .sorted(Comparator.comparing(OrderRefund::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private void validateRefundRequest(PurchaseOrder order, BigDecimal amount) {
        if (order.getPaymentStatus() != OrderPaymentStatus.PAID) {
            throw new IllegalArgumentException("Only paid orders can be refunded");
        }
        if (!CashfreeApiConstants.GATEWAY_NAME.equalsIgnoreCase(order.getPaymentProvider())) {
            throw new IllegalArgumentException("Only Cashfree payments can be refunded from this application");
        }
        BigDecimal refundableAmount = summarize(order).refundableAmount();
        if (amount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds the remaining refundable amount");
        }
    }

    private void applyGatewayResult(
            OrderRefund refund,
            PurchaseOrder order,
            BigDecimal amount,
            String refundId,
            String note,
            String speed
    ) {
        CashfreeRefundResult result = cashfreeGatewayService.createRefund(order, amount, refundId, note, speed);
        refund.applyGatewayStatus(
                result.providerRefundId(),
                result.providerPaymentId(),
                result.status(),
                result.statusDescription(),
                result.refundArn(),
                result.processedAt()
        );
        addRefundHistory(order, refund);
    }

    private OrderRefund findRefund(RefundWebhookUpdate update) {
        return orderRefundRepository.findByRefundId(update.refundId())
                .or(() -> hasText(update.providerRefundId())
                        ? orderRefundRepository.findByProviderRefundId(update.providerRefundId())
                        : java.util.Optional.empty())
                .orElse(null);
    }

    private OrderRefund createWebhookRefund(PurchaseOrder order, RefundWebhookUpdate update) {
        return new OrderRefund(
                order,
                update.refundId(),
                update.amount(),
                hasText(update.currency()) ? update.currency() : order.getCurrency(),
                hasText(update.speed()) ? update.speed() : DEFAULT_REFUND_SPEED,
                truncate(hasText(update.note()) ? update.note() : DEFAULT_REFUND_NOTE, 100),
                WEBHOOK_ACTOR
        );
    }

    private void validateWebhookUpdate(RefundWebhookUpdate update) {
        if (!hasText(update.orderNumber()) || !hasText(update.refundId())) {
            throw new IllegalArgumentException("Missing Cashfree refund reference");
        }
        if (update.amount() == null || update.amount().signum() <= 0 || update.status() == null) {
            throw new IllegalArgumentException("Invalid Cashfree refund payload");
        }
    }

    private void addRefundHistory(PurchaseOrder order, OrderRefund refund) {
        String note = "Refund " + refund.getRefundId() + " is "
                + refund.getStatus().name().toLowerCase(Locale.ROOT) + ".";
        order.addStatusHistory(new OrderStatusHistory(order.getStatus(), note));
    }

    private String resolveSummaryStatus(
            List<OrderRefund> refunds,
            BigDecimal paidAmount,
            BigDecimal refundedAmount
    ) {
        if (refunds.isEmpty()) {
            return "NOT_REQUESTED";
        }
        if (refunds.stream().anyMatch(refund -> refund.getStatus() == OrderRefundStatus.ONHOLD)) {
            return "ON_HOLD";
        }
        if (refunds.stream().anyMatch(refund -> refund.getStatus() == OrderRefundStatus.PENDING)) {
            return "PENDING";
        }
        if (paidAmount.signum() > 0 && refundedAmount.compareTo(paidAmount) >= 0) {
            return "REFUNDED";
        }
        if (refundedAmount.signum() > 0) {
            return "PARTIALLY_REFUNDED";
        }
        return refunds.stream().anyMatch(refund -> refund.getStatus() == OrderRefundStatus.FAILED)
                ? "FAILED"
                : "CANCELLED";
    }

    private BigDecimal sumByStatus(List<OrderRefund> refunds, OrderRefundStatus status) {
        return refunds.stream()
                .filter(refund -> refund.getStatus() == status)
                .map(OrderRefund::getAmount)
                .reduce(ZERO, BigDecimal::add);
    }

    private boolean reservesRefundAmount(OrderRefund refund) {
        return refund.getStatus() == OrderRefundStatus.PENDING
                || refund.getStatus() == OrderRefundStatus.ONHOLD;
    }

    private BigDecimal resolvePaidAmount(PurchaseOrder order) {
        if (order.getPaymentStatus() != OrderPaymentStatus.PAID) {
            return ZERO;
        }
        BigDecimal amount = order.getPaymentDueAmount() != null
                ? order.getPaymentDueAmount()
                : order.getTotalAmount();
        return amount == null ? ZERO : amount;
    }

    private String normalizeSpeed(String value) {
        String speed = hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : DEFAULT_REFUND_SPEED;
        if (!REFUND_SPEEDS.contains(speed)) {
            throw new IllegalArgumentException("Refund speed must be STANDARD or INSTANT");
        }
        return speed;
    }

    private String normalizeNote(String value) {
        return hasText(value) ? value.trim() : DEFAULT_REFUND_NOTE;
    }

    private String buildRefundId(Long orderId) {
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "REF-" + orderId + "-" + uniquePart;
    }

    private OrderRefundResponse toResponse(OrderRefund refund) {
        return new OrderRefundResponse(
                refund.getId(),
                refund.getRefundId(),
                refund.getProviderRefundId(),
                refund.getProviderPaymentId(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getSpeed(),
                refund.getNote(),
                refund.getStatusDescription(),
                refund.getRefundArn(),
                refund.getRequestedBy(),
                refund.getCreatedAt(),
                refund.getUpdatedAt(),
                refund.getProcessedAt()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

}

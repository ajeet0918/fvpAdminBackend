package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.dto.CashfreeCreateOrderResult;
import com.agriplatform.backend.payment.dto.CashfreeOrderStatusResult;
import com.agriplatform.backend.payment.dto.CashfreeRefundResult;
import com.agriplatform.backend.payment.dto.CashfreeRefundSnapshot;
import com.agriplatform.backend.payment.model.OrderRefundStatus;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.model.CreateOrderRequest;
import com.cashfree.pg.model.CustomerDetails;
import com.cashfree.pg.model.OrderEntity;
import com.cashfree.pg.model.OrderMeta;
import com.cashfree.pg.model.OrderCreateRefundRequest;
import com.cashfree.pg.model.PaymentEntity;
import com.cashfree.pg.model.RefundEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CashfreeGatewayService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashfreeGatewayService.class);

    private final CashfreeSettingsResolver cashfreeSettingsResolver;
    private final CashfreeClientFactory cashfreeClientFactory;

    public CashfreeGatewayService(
            CashfreeSettingsResolver cashfreeSettingsResolver,
            CashfreeClientFactory cashfreeClientFactory
    ) {
        this.cashfreeSettingsResolver = cashfreeSettingsResolver;
        this.cashfreeClientFactory = cashfreeClientFactory;
    }

    public boolean isEnabled() {
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        return config.enabled() && config.hasCredentials();
    }

    public CashfreeCreateOrderResult createOrder(
            PurchaseOrder order,
            Customer customer,
            BigDecimal amount,
            String checkoutSuccessUrl,
            String checkoutFailureUrl
    ) {
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        if (!config.enabled() || !config.hasCredentials()) {
            throw new IllegalStateException("Cashfree gateway is disabled or not configured");
        }

        try {
            Cashfree cashfree = cashfreeClientFactory.create(config);

            CreateOrderRequest request = new CreateOrderRequest()
                    .orderId(order.getOrderNumber())
                    .orderCurrency(CashfreeApiConstants.CURRENCY_INR)
                    .orderAmount(amount)
                    .customerDetails(buildCustomer(customer))
                    .orderMeta(buildOrderMeta(config, checkoutSuccessUrl, checkoutFailureUrl));

            ApiResponse<OrderEntity> response = cashfree.PGCreateOrder(
                    request,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID(),
                    null
            );
            OrderEntity entity = response.getData();
            if (entity == null || !hasText(entity.getCfOrderId()) || !hasText(entity.getPaymentSessionId())) {
                throw new IllegalStateException("Cashfree response missing required order fields");
            }

            return new CashfreeCreateOrderResult(
                    entity.getCfOrderId(),
                    entity.getPaymentSessionId(),
                    ""
            );
        } catch (ApiException ex) {
            String responseBody = ex.getResponseBody();
            String errorMessage = hasText(responseBody) ? responseBody : ex.getMessage();
            throw new IllegalStateException("Cashfree order creation failed: " + errorMessage, ex);
        }
    }

    public Optional<CashfreeOrderStatusResult> findOrder(PurchaseOrder order) {
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        requireEnabledGateway(config);

        try {
            Cashfree cashfree = cashfreeClientFactory.create(config);
            ApiResponse<OrderEntity> response = cashfree.PGFetchOrder(
                    order.getOrderNumber(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID(),
                    null
            );
            OrderEntity entity = response.getData();
            validateGatewayOrder(order, entity);
            return Optional.of(toStatusResult(cashfree, order.getOrderNumber(), entity));
        } catch (ApiException ex) {
            if (ex.getCode() == CashfreeApiConstants.HTTP_STATUS_NOT_FOUND) {
                return Optional.empty();
            }
            throw gatewayFailure("Cashfree order lookup failed", ex);
        }
    }

    public CashfreeRefundResult createRefund(
            PurchaseOrder order,
            BigDecimal amount,
            String refundId,
            String note,
            String speed
    ) {
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        requireEnabledGateway(config);

        try {
            Cashfree cashfree = cashfreeClientFactory.create(config);
            OrderCreateRefundRequest request = new OrderCreateRefundRequest()
                    .refundAmount(amount)
                    .refundId(refundId)
                    .refundNote(note)
                    .refundSpeed(OrderCreateRefundRequest.RefundSpeedEnum.valueOf(speed));
            ApiResponse<RefundEntity> response = cashfree.PGOrderCreateRefund(
                    order.getOrderNumber(),
                    request,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID(),
                    null
            );
            return toRefundResult(response.getData());
        } catch (ApiException ex) {
            throw gatewayFailure("Cashfree refund creation failed", ex);
        }
    }

    public List<CashfreeRefundSnapshot> fetchRefunds(PurchaseOrder order) {
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        requireEnabledGateway(config);
        try {
            Cashfree cashfree = cashfreeClientFactory.create(config);
            ApiResponse<List<RefundEntity>> response = cashfree.PGOrderFetchRefunds(
                    order.getOrderNumber(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID(),
                    null
            );
            List<RefundEntity> refunds = response.getData();
            return refunds == null ? List.of() : refunds.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(this::toRefundSnapshot)
                    .toList();
        } catch (ApiException ex) {
            throw gatewayFailure("Cashfree refund lookup failed", ex);
        }
    }

    private CustomerDetails buildCustomer(Customer customer) {
        return new CustomerDetails()
                .customerId(String.valueOf(customer.getId()))
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone());
    }

    private CashfreeRefundResult toRefundResult(RefundEntity entity) {
        if (entity == null || entity.getRefundStatus() == null || !hasText(entity.getCfRefundId())) {
            throw new IllegalStateException("Cashfree refund response is incomplete");
        }
        return new CashfreeRefundResult(
                entity.getCfRefundId(),
                entity.getCfPaymentId(),
                OrderRefundStatus.valueOf(entity.getRefundStatus().name()),
                entity.getStatusDescription(),
                entity.getRefundArn(),
                parseGatewayTime(entity.getProcessedAt())
        );
    }

    private CashfreeRefundSnapshot toRefundSnapshot(RefundEntity entity) {
        if (!hasText(entity.getRefundId()) || !hasText(entity.getCfRefundId())
                || entity.getRefundAmount() == null || entity.getRefundStatus() == null) {
            throw new IllegalStateException("Cashfree refund lookup returned incomplete data");
        }
        return new CashfreeRefundSnapshot(
                entity.getRefundId(),
                entity.getCfRefundId(),
                entity.getCfPaymentId(),
                entity.getRefundAmount(),
                entity.getRefundCurrency(),
                OrderRefundStatus.valueOf(entity.getRefundStatus().name()),
                hasText(entity.getRefundMode()) ? entity.getRefundMode() : "STANDARD",
                entity.getRefundNote(),
                entity.getStatusDescription(),
                entity.getRefundArn(),
                parseGatewayTime(entity.getProcessedAt())
        );
    }

    private LocalDateTime parseGatewayTime(String value) {
        if (!hasText(value)) {
            return null;
        }
        return OffsetDateTime.parse(value).toLocalDateTime();
    }

    private CashfreeOrderStatusResult toStatusResult(
            Cashfree cashfree,
            String orderNumber,
            OrderEntity entity
    ) {
        String paymentReference = CashfreeApiConstants.ORDER_STATUS_PAID.equalsIgnoreCase(entity.getOrderStatus())
                ? findSuccessfulPaymentReference(cashfree, orderNumber, entity.getCfOrderId())
                : "";
        return new CashfreeOrderStatusResult(
                entity.getCfOrderId(),
                entity.getPaymentSessionId(),
                entity.getOrderStatus(),
                paymentReference
        );
    }

    private String findSuccessfulPaymentReference(Cashfree cashfree, String orderNumber, String fallbackReference) {
        try {
            ApiResponse<List<PaymentEntity>> response = cashfree.PGOrderFetchPayments(
                    orderNumber,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID(),
                    null
            );
            List<PaymentEntity> payments = response.getData();
            if (payments == null) {
                return fallbackReference;
            }
            return payments.stream()
                    .filter(java.util.Objects::nonNull)
                    .filter(payment -> payment.getPaymentStatus() == PaymentEntity.PaymentStatusEnum.SUCCESS)
                    .map(PaymentEntity::getCfPaymentId)
                    .filter(this::hasText)
                    .findFirst()
                    .orElse(fallbackReference);
        } catch (ApiException ex) {
            log.warn("Unable to fetch successful Cashfree payment reference for order {}", orderNumber);
            return fallbackReference;
        }
    }

    private void validateGatewayOrder(PurchaseOrder order, OrderEntity entity) {
        if (entity == null || !hasText(entity.getCfOrderId()) || !hasText(entity.getOrderStatus())) {
            throw new IllegalStateException("Cashfree order lookup returned incomplete data");
        }
        if (entity.getOrderAmount() == null || order.getTotalAmount() == null
                || entity.getOrderAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalStateException("Cashfree order amount does not match the application order");
        }
        if (!hasText(entity.getOrderCurrency()) || !entity.getOrderCurrency().equalsIgnoreCase(order.getCurrency())) {
            throw new IllegalStateException("Cashfree order currency does not match the application order");
        }
    }

    private void requireEnabledGateway(CashfreeRuntimeConfig config) {
        if (!config.enabled() || !config.hasCredentials()) {
            throw new IllegalStateException("Cashfree gateway is disabled or not configured");
        }
    }

    private IllegalStateException gatewayFailure(String message, ApiException ex) {
        String responseBody = ex.getResponseBody();
        String errorMessage = hasText(responseBody) ? responseBody : ex.getMessage();
        return new IllegalStateException(message + ": " + errorMessage, ex);
    }

    private OrderMeta buildOrderMeta(CashfreeRuntimeConfig config, String checkoutSuccessUrl, String checkoutFailureUrl) {
        String fallbackUrl = hasText(checkoutFailureUrl) ? checkoutFailureUrl : checkoutSuccessUrl;
        String returnUrl = hasText(checkoutSuccessUrl)
                ? checkoutSuccessUrl + buildQueryDelimiter(checkoutSuccessUrl) + "order_id={order_id}"
                : fallbackUrl;

        return new OrderMeta()
                .returnUrl(returnUrl);
    }

    private String buildQueryDelimiter(String url) {
        return url.contains("?") ? "&" : "?";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

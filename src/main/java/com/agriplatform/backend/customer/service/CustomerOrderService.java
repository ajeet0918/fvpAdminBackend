package com.agriplatform.backend.customer.service;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.customer.repository.CustomerRepository;
import com.agriplatform.backend.order.dto.CreateOrderPaymentSessionRequest;
import com.agriplatform.backend.order.dto.CreateCustomerOrderRequest;
import com.agriplatform.backend.order.dto.OrderPaymentSessionResponse;
import com.agriplatform.backend.order.dto.OrderResponse;
import com.agriplatform.backend.order.model.OrderPaymentStatus;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.service.OrderService;
import com.agriplatform.backend.payment.dto.CashfreeCreateOrderResult;
import com.agriplatform.backend.payment.dto.CashfreeOrderStatusResult;
import com.agriplatform.backend.payment.service.CashfreeApiConstants;
import com.agriplatform.backend.payment.service.CashfreeGatewayService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CustomerOrderService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerOrderService.class);

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final CashfreeGatewayService cashfreeGatewayService;

    public CustomerOrderService(
            OrderService orderService,
            CustomerRepository customerRepository,
            CashfreeGatewayService cashfreeGatewayService
    ) {
        this.orderService = orderService;
        this.customerRepository = customerRepository;
        this.cashfreeGatewayService = cashfreeGatewayService;
    }

    public OrderPaymentSessionResponse createDirectOrder(Long customerId, CreateCustomerOrderRequest request) {
        OrderResponse orderResponse = orderService.createOrderForCustomer(customerId, request);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Persist payment intent first so order lifecycle is not lost if gateway is unavailable.
        PurchaseOrder pendingOrder = orderService.markPaymentPending(
                orderResponse.id(),
                CashfreeApiConstants.GATEWAY_NAME,
                null,
                orderResponse.totalAmount(),
                "Payment initiation requested."
        );

        if (!cashfreeGatewayService.isEnabled()) {
            return new OrderPaymentSessionResponse(
                    pendingOrder.getId(),
                    pendingOrder.getOrderNumber(),
                    CashfreeApiConstants.GATEWAY_NAME,
                    null,
                    "",
                    "",
                    "Gateway is disabled. Order created and payment can be initiated later."
            );
        }

        return tryCreateGatewaySession(
                pendingOrder,
                customer,
                request.checkoutSuccessUrl(),
                request.checkoutFailureUrl(),
                "Order created. Payment session could not be created now. Please retry from My Account."
        );
    }

    public OrderPaymentSessionResponse retryPaymentSession(Long customerId, Long orderId, CreateOrderPaymentSessionRequest request) {
        PurchaseOrder order = orderService.getOrderEntityForOperations(orderId);
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to this customer");
        }
        if (order.getPaymentStatus() == OrderPaymentStatus.PAID) {
            throw new IllegalArgumentException("Payment already completed for this order");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        return resolveRetryPaymentSession(order, customer, request);
    }

    public List<OrderResponse> getOrderHistory(Long customerId) {
        return orderService.getOrdersForCustomer(customerId);
    }

    private OrderPaymentSessionResponse tryCreateGatewaySession(
            PurchaseOrder order,
            Customer customer,
            String successUrl,
            String failureUrl,
            String failureMessage
    ) {
        try {
            CashfreeCreateOrderResult paymentResult = cashfreeGatewayService.createOrder(
                    order,
                    customer,
                    order.getTotalAmount(),
                    successUrl,
                    failureUrl
            );

            PurchaseOrder updatedOrder = orderService.markPaymentPending(
                    order.getId(),
                    CashfreeApiConstants.GATEWAY_NAME,
                    paymentResult.providerOrderId(),
                    order.getTotalAmount(),
                    "Payment session created."
            );

            return new OrderPaymentSessionResponse(
                    updatedOrder.getId(),
                    updatedOrder.getOrderNumber(),
                    CashfreeApiConstants.GATEWAY_NAME,
                    paymentResult.providerOrderId(),
                    paymentResult.paymentSessionId(),
                    paymentResult.paymentLink(),
                    "Payment session created successfully."
            );
        } catch (RuntimeException ex) {
            orderService.addPaymentStatusHistory(
                    order.getId(),
                    "Payment gateway session creation failed: " + safeMessage(ex)
            );
            PurchaseOrder latest = orderService.getOrderEntityForOperations(order.getId());
            return new OrderPaymentSessionResponse(
                    latest.getId(),
                    latest.getOrderNumber(),
                    CashfreeApiConstants.GATEWAY_NAME,
                    latest.getPaymentProviderOrderId(),
                    "",
                    "",
                    failureMessage
            );
        }
    }

    private OrderPaymentSessionResponse resolveRetryPaymentSession(
            PurchaseOrder order,
            Customer customer,
            CreateOrderPaymentSessionRequest request
    ) {
        Optional<CashfreeOrderStatusResult> gatewayOrder;
        try {
            gatewayOrder = cashfreeGatewayService.findOrder(order);
        } catch (RuntimeException ex) {
            return paymentSessionFailure(order, ex);
        }

        if (gatewayOrder.isPresent()) {
            return handleExistingGatewayOrder(order, gatewayOrder.get());
        }

        PurchaseOrder pendingOrder = orderService.markPaymentPending(
                order.getId(),
                CashfreeApiConstants.GATEWAY_NAME,
                null,
                order.getTotalAmount(),
                "Payment retry requested; no existing Cashfree order was found."
        );
        return tryCreateGatewaySession(
                pendingOrder,
                customer,
                request.checkoutSuccessUrl(),
                request.checkoutFailureUrl(),
                "Unable to create payment session right now. Please retry after some time."
        );
    }

    private OrderPaymentSessionResponse handleExistingGatewayOrder(
            PurchaseOrder order,
            CashfreeOrderStatusResult gatewayOrder
    ) {
        if (CashfreeApiConstants.ORDER_STATUS_PAID.equalsIgnoreCase(gatewayOrder.orderStatus())) {
            PurchaseOrder paidOrder = orderService.markPaymentSuccessByGatewayOrderReference(
                    order.getOrderNumber(),
                    gatewayOrder.paymentReference()
            );
            return paymentSessionResponse(paidOrder, gatewayOrder, "", "Payment is already confirmed.");
        }
        if (CashfreeApiConstants.ORDER_STATUS_ACTIVE.equalsIgnoreCase(gatewayOrder.orderStatus())
                && hasText(gatewayOrder.paymentSessionId())) {
            PurchaseOrder pendingOrder = orderService.markPaymentPending(
                    order.getId(),
                    CashfreeApiConstants.GATEWAY_NAME,
                    gatewayOrder.providerOrderId(),
                    order.getTotalAmount(),
                    "Existing Cashfree payment session reused."
            );
            if (pendingOrder.getPaymentStatus() == OrderPaymentStatus.PAID) {
                return paymentSessionResponse(pendingOrder, gatewayOrder, "", "Payment is already confirmed.");
            }
            return paymentSessionResponse(
                    pendingOrder,
                    gatewayOrder,
                    gatewayOrder.paymentSessionId(),
                    "Existing payment session resumed."
            );
        }

        orderService.addPaymentStatusHistory(order.getId(), "Cashfree payment session is no longer payable.");
        return paymentSessionResponse(
                order,
                gatewayOrder,
                "",
                "The previous payment session has expired. Contact support."
        );
    }

    private OrderPaymentSessionResponse paymentSessionFailure(PurchaseOrder order, RuntimeException ex) {
        orderService.addPaymentStatusHistory(
                order.getId(),
                "Payment status reconciliation failed: " + safeMessage(ex)
        );
        return new OrderPaymentSessionResponse(
                order.getId(),
                order.getOrderNumber(),
                CashfreeApiConstants.GATEWAY_NAME,
                order.getPaymentProviderOrderId(),
                "",
                "",
                "Unable to verify the previous payment right now. Please retry after some time."
        );
    }

    private OrderPaymentSessionResponse paymentSessionResponse(
            PurchaseOrder order,
            CashfreeOrderStatusResult gatewayOrder,
            String paymentSessionId,
            String message
    ) {
        return new OrderPaymentSessionResponse(
                order.getId(),
                order.getOrderNumber(),
                CashfreeApiConstants.GATEWAY_NAME,
                gatewayOrder.providerOrderId(),
                paymentSessionId,
                "",
                message
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safeMessage(RuntimeException ex) {
        if (ex.getMessage() == null || ex.getMessage().isBlank()) {
            return "unknown gateway error";
        }
        return ex.getMessage();
    }
}

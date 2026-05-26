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
import com.agriplatform.backend.payment.service.CashfreeApiConstants;
import com.agriplatform.backend.payment.service.CashfreeGatewayService;
import java.util.List;
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
                orderResponse.totalAmount()
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

        // Keep order in pending state and retry gateway session creation.
        orderService.markPaymentPending(
                order.getId(),
                CashfreeApiConstants.GATEWAY_NAME,
                order.getPaymentProviderOrderId(),
                order.getTotalAmount()
        );

        return tryCreateGatewaySession(
                orderService.getOrderEntityForOperations(orderId),
                customer,
                request.checkoutSuccessUrl(),
                request.checkoutFailureUrl(),
                "Unable to create payment session right now. Please retry after some time."
        );
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
                    order.getTotalAmount()
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

    private String safeMessage(RuntimeException ex) {
        if (ex.getMessage() == null || ex.getMessage().isBlank()) {
            return "unknown gateway error";
        }
        return ex.getMessage();
    }
}

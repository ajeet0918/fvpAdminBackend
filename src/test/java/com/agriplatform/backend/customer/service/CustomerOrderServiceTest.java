package com.agriplatform.backend.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.customer.repository.CustomerRepository;
import com.agriplatform.backend.order.dto.CreateOrderPaymentSessionRequest;
import com.agriplatform.backend.order.dto.OrderPaymentSessionResponse;
import com.agriplatform.backend.order.model.OrderPaymentStatus;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.service.OrderService;
import com.agriplatform.backend.payment.dto.CashfreeCreateOrderResult;
import com.agriplatform.backend.payment.dto.CashfreeOrderStatusResult;
import com.agriplatform.backend.payment.service.CashfreeGatewayService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerOrderServiceTest {
    private static final long CUSTOMER_ID = 7L;
    private static final long ORDER_ID = 11L;
    private static final String ORDER_NUMBER = "FVP-ORDER-1";
    private static final String SUCCESS_URL = "https://www.fvppurepick.com/portal/orders";
    private static final String FAILURE_URL = "https://www.fvppurepick.com/portal/orders";

    private OrderService orderService;
    private CustomerRepository customerRepository;
    private CashfreeGatewayService cashfreeGatewayService;
    private CustomerOrderService customerOrderService;
    private PurchaseOrder order;
    private Customer customer;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        customerRepository = mock(CustomerRepository.class);
        cashfreeGatewayService = mock(CashfreeGatewayService.class);
        customerOrderService = new CustomerOrderService(orderService, customerRepository, cashfreeGatewayService);
        order = mock(PurchaseOrder.class);
        customer = mock(Customer.class);
    }

    @Test
    void retryPaymentReusesActiveCashfreeSessionAfterPopupIsClosed() {
        stubRetryOrder();
        CashfreeOrderStatusResult gatewayOrder = gatewayOrder("ACTIVE", "payment-session-id", "");
        when(cashfreeGatewayService.findOrder(order)).thenReturn(Optional.of(gatewayOrder));
        when(orderService.markPaymentPending(
                eq(ORDER_ID), anyString(), anyString(), any(BigDecimal.class), anyString()
        )).thenReturn(order);

        OrderPaymentSessionResponse response = customerOrderService.retryPaymentSession(
                CUSTOMER_ID,
                ORDER_ID,
                request()
        );

        assertThat(response.paymentSessionId()).isEqualTo("payment-session-id");
        assertThat(response.message()).isEqualTo("Existing payment session resumed.");
        verify(cashfreeGatewayService, never()).createOrder(any(), any(), any(), anyString(), anyString());
    }

    @Test
    void retryPaymentReconcilesPaidCashfreeOrderWithoutOpeningCheckout() {
        stubRetryOrder();
        CashfreeOrderStatusResult gatewayOrder = gatewayOrder("PAID", "payment-session-id", "payment-id");
        when(cashfreeGatewayService.findOrder(order)).thenReturn(Optional.of(gatewayOrder));
        when(orderService.markPaymentSuccessByGatewayOrderReference(ORDER_NUMBER, "payment-id")).thenReturn(order);

        OrderPaymentSessionResponse response = customerOrderService.retryPaymentSession(
                CUSTOMER_ID,
                ORDER_ID,
                request()
        );

        assertThat(response.paymentSessionId()).isEmpty();
        assertThat(response.message()).isEqualTo("Payment is already confirmed.");
        verify(cashfreeGatewayService, never()).createOrder(any(), any(), any(), anyString(), anyString());
    }

    @Test
    void retryPaymentDoesNotOpenCheckoutWhenWebhookWinsConcurrentUpdate() {
        stubRetryOrder();
        PurchaseOrder paidOrder = mock(PurchaseOrder.class);
        when(paidOrder.getId()).thenReturn(ORDER_ID);
        when(paidOrder.getOrderNumber()).thenReturn(ORDER_NUMBER);
        when(paidOrder.getPaymentStatus()).thenReturn(OrderPaymentStatus.PAID);
        CashfreeOrderStatusResult gatewayOrder = gatewayOrder("ACTIVE", "payment-session-id", "");
        when(cashfreeGatewayService.findOrder(order)).thenReturn(Optional.of(gatewayOrder));
        when(orderService.markPaymentPending(
                eq(ORDER_ID), anyString(), anyString(), any(BigDecimal.class), anyString()
        )).thenReturn(paidOrder);

        OrderPaymentSessionResponse response = customerOrderService.retryPaymentSession(
                CUSTOMER_ID,
                ORDER_ID,
                request()
        );

        assertThat(response.paymentSessionId()).isEmpty();
        assertThat(response.message()).isEqualTo("Payment is already confirmed.");
    }

    @Test
    void retryPaymentCreatesOrderOnlyWhenCashfreeOrderDoesNotExist() {
        stubRetryOrder();
        when(cashfreeGatewayService.findOrder(order)).thenReturn(Optional.empty());
        when(orderService.markPaymentPending(
                eq(ORDER_ID), anyString(), isNull(), any(BigDecimal.class), anyString()
        )).thenReturn(order);
        when(orderService.markPaymentPending(
                eq(ORDER_ID), anyString(), eq("cashfree-order-id"), any(BigDecimal.class), anyString()
        )).thenReturn(order);
        when(cashfreeGatewayService.createOrder(order, customer, new BigDecimal("100.00"), SUCCESS_URL, FAILURE_URL))
                .thenReturn(new CashfreeCreateOrderResult("cashfree-order-id", "new-session-id", ""));

        OrderPaymentSessionResponse response = customerOrderService.retryPaymentSession(
                CUSTOMER_ID,
                ORDER_ID,
                request()
        );

        assertThat(response.paymentSessionId()).isEqualTo("new-session-id");
        verify(cashfreeGatewayService).createOrder(order, customer, new BigDecimal("100.00"), SUCCESS_URL, FAILURE_URL);
    }

    @Test
    void retryPaymentDoesNotCreateDuplicateForExpiredCashfreeOrder() {
        stubRetryOrder();
        CashfreeOrderStatusResult gatewayOrder = gatewayOrder("EXPIRED", "", "");
        when(cashfreeGatewayService.findOrder(order)).thenReturn(Optional.of(gatewayOrder));

        OrderPaymentSessionResponse response = customerOrderService.retryPaymentSession(
                CUSTOMER_ID,
                ORDER_ID,
                request()
        );

        assertThat(response.paymentSessionId()).isEmpty();
        assertThat(response.message()).contains("expired");
        verify(cashfreeGatewayService, never()).createOrder(any(), any(), any(), anyString(), anyString());
    }

    @Test
    void retryPaymentDoesNotCreateDuplicateWhenReconciliationFails() {
        stubRetryOrder();
        when(cashfreeGatewayService.findOrder(order)).thenThrow(new IllegalStateException("gateway unavailable"));

        OrderPaymentSessionResponse response = customerOrderService.retryPaymentSession(
                CUSTOMER_ID,
                ORDER_ID,
                request()
        );

        assertThat(response.paymentSessionId()).isEmpty();
        assertThat(response.message()).contains("Unable to verify");
        verify(cashfreeGatewayService, never()).createOrder(any(), any(), any(), anyString(), anyString());
    }

    private void stubRetryOrder() {
        when(orderService.getOrderEntityForOperations(ORDER_ID)).thenReturn(order);
        when(order.getId()).thenReturn(ORDER_ID);
        when(order.getOrderNumber()).thenReturn(ORDER_NUMBER);
        when(order.getCustomer()).thenReturn(customer);
        when(order.getPaymentStatus()).thenReturn(OrderPaymentStatus.PENDING);
        when(order.getTotalAmount()).thenReturn(new BigDecimal("100.00"));
        when(customer.getId()).thenReturn(CUSTOMER_ID);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    }

    private CashfreeOrderStatusResult gatewayOrder(String status, String sessionId, String paymentReference) {
        return new CashfreeOrderStatusResult("cashfree-order-id", sessionId, status, paymentReference);
    }

    private CreateOrderPaymentSessionRequest request() {
        return new CreateOrderPaymentSessionRequest(SUCCESS_URL, FAILURE_URL);
    }
}

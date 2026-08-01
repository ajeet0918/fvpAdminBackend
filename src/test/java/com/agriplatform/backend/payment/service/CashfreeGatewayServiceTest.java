package com.agriplatform.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

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
import com.cashfree.pg.model.OrderEntity;
import com.cashfree.pg.model.PaymentEntity;
import com.cashfree.pg.model.RefundEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CashfreeGatewayServiceTest {

    @Mock
    private CashfreeSettingsResolver cashfreeSettingsResolver;

    @Mock
    private CashfreeClientFactory cashfreeClientFactory;

    @Mock
    private Cashfree cashfree;

    @Mock
    private ApiResponse<OrderEntity> apiResponse;

    @Mock
    private ApiResponse<List<PaymentEntity>> paymentApiResponse;

    @Mock
    private PurchaseOrder order;

    @Mock
    private Customer customer;

    @Test
    void createOrderUsesConfiguredSdkClientAndReturnsPaymentSession() throws ApiException {
        CashfreeRuntimeConfig config = runtimeConfig();
        OrderEntity entity = new OrderEntity()
                .cfOrderId("cashfree-order-id")
                .paymentSessionId("payment-session-id");
        when(cashfreeSettingsResolver.resolve()).thenReturn(config);
        when(cashfreeClientFactory.create(config)).thenReturn(cashfree);
        when(order.getOrderNumber()).thenReturn("FVP-ORDER-1");
        when(customer.getId()).thenReturn(7L);
        when(customer.getFullName()).thenReturn("Test Customer");
        when(customer.getEmail()).thenReturn("customer@example.com");
        when(customer.getPhone()).thenReturn("9999999999");
        when(cashfree.PGCreateOrder(any(), anyString(), any(UUID.class), isNull())).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(entity);

        CashfreeGatewayService service = new CashfreeGatewayService(
                cashfreeSettingsResolver,
                cashfreeClientFactory
        );
        CashfreeCreateOrderResult result = service.createOrder(
                order,
                customer,
                new BigDecimal("100.00"),
                "https://www.fvppurepick.com/portal/orders",
                "https://www.fvppurepick.com/checkout"
        );

        assertThat(result.providerOrderId()).isEqualTo("cashfree-order-id");
        assertThat(result.paymentSessionId()).isEqualTo("payment-session-id");
        verify(cashfreeClientFactory).create(config);
        verify(cashfree).PGCreateOrder(any(), anyString(), any(UUID.class), isNull());
    }

    @Test
    void findOrderReturnsExistingActivePaymentSession() throws ApiException {
        OrderEntity entity = gatewayOrder("ACTIVE");
        stubGatewayOrder(entity);

        Optional<CashfreeOrderStatusResult> result = service().findOrder(order);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().paymentSessionId()).isEqualTo("payment-session-id");
        verify(cashfree, never()).PGOrderFetchPayments(anyString(), anyString(), any(UUID.class), isNull());
    }

    @Test
    void findOrderReturnsSuccessfulPaymentReferenceForPaidOrder() throws ApiException {
        OrderEntity entity = gatewayOrder("PAID");
        PaymentEntity payment = new PaymentEntity()
                .paymentStatus(PaymentEntity.PaymentStatusEnum.SUCCESS)
                .cfPaymentId("cashfree-payment-id");
        stubGatewayOrder(entity);
        when(cashfree.PGOrderFetchPayments(anyString(), anyString(), any(UUID.class), isNull()))
                .thenReturn(paymentApiResponse);
        when(paymentApiResponse.getData()).thenReturn(List.of(payment));

        CashfreeOrderStatusResult result = service().findOrder(order).orElseThrow();

        assertThat(result.orderStatus()).isEqualTo("PAID");
        assertThat(result.paymentReference()).isEqualTo("cashfree-payment-id");
    }

    @Test
    void createRefundUsesCashfreeSdkAndReturnsGatewayStatus() throws ApiException {
        RefundEntity refundEntity = new RefundEntity()
                .cfRefundId("cashfree-refund-id")
                .cfPaymentId("cashfree-payment-id")
                .refundStatus(RefundEntity.RefundStatusEnum.PENDING)
                .statusDescription("Refund initiated");
        ApiResponse<RefundEntity> refundResponse = org.mockito.Mockito.mock(ApiResponse.class);
        when(cashfreeSettingsResolver.resolve()).thenReturn(runtimeConfig());
        when(cashfreeClientFactory.create(any())).thenReturn(cashfree);
        when(order.getOrderNumber()).thenReturn("FVP-ORDER-1");
        when(cashfree.PGOrderCreateRefund(anyString(), any(), anyString(), any(UUID.class), isNull()))
                .thenReturn(refundResponse);
        when(refundResponse.getData()).thenReturn(refundEntity);

        CashfreeRefundResult result = service().createRefund(
                order,
                new BigDecimal("40.00"),
                "REF-ORDER-1",
                "Cancelled before dispatch",
                "STANDARD"
        );

        assertThat(result.providerRefundId()).isEqualTo("cashfree-refund-id");
        assertThat(result.status()).isEqualTo(OrderRefundStatus.PENDING);
        verify(cashfree).PGOrderCreateRefund(anyString(), any(), anyString(), any(UUID.class), isNull());
    }

    @Test
    void fetchRefundsMapsExistingCashfreeRefunds() throws ApiException {
        RefundEntity refundEntity = new RefundEntity()
                .refundId("REF-ORDER-1")
                .cfRefundId("cashfree-refund-id")
                .cfPaymentId("cashfree-payment-id")
                .refundAmount(new BigDecimal("40.00"))
                .refundCurrency("INR")
                .refundStatus(RefundEntity.RefundStatusEnum.SUCCESS)
                .refundNote("Cancelled before dispatch");
        ApiResponse<List<RefundEntity>> refundResponse = org.mockito.Mockito.mock(ApiResponse.class);
        when(cashfreeSettingsResolver.resolve()).thenReturn(runtimeConfig());
        when(cashfreeClientFactory.create(any())).thenReturn(cashfree);
        when(order.getOrderNumber()).thenReturn("FVP-ORDER-1");
        when(cashfree.PGOrderFetchRefunds(anyString(), anyString(), any(UUID.class), isNull()))
                .thenReturn(refundResponse);
        when(refundResponse.getData()).thenReturn(List.of(refundEntity));

        List<CashfreeRefundSnapshot> refunds = service().fetchRefunds(order);

        assertThat(refunds).hasSize(1);
        assertThat(refunds.getFirst().status()).isEqualTo(OrderRefundStatus.SUCCESS);
        assertThat(refunds.getFirst().amount()).isEqualByComparingTo("40.00");
    }

    private void stubGatewayOrder(OrderEntity entity) throws ApiException {
        when(cashfreeSettingsResolver.resolve()).thenReturn(runtimeConfig());
        when(cashfreeClientFactory.create(any())).thenReturn(cashfree);
        when(order.getOrderNumber()).thenReturn("FVP-ORDER-1");
        when(order.getTotalAmount()).thenReturn(new BigDecimal("100.00"));
        when(order.getCurrency()).thenReturn("INR");
        when(cashfree.PGFetchOrder(anyString(), anyString(), any(UUID.class), isNull())).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(entity);
    }

    private OrderEntity gatewayOrder(String status) {
        return new OrderEntity()
                .cfOrderId("cashfree-order-id")
                .paymentSessionId("payment-session-id")
                .orderStatus(status)
                .orderAmount(new BigDecimal("100.00"))
                .orderCurrency("INR");
    }

    private CashfreeGatewayService service() {
        return new CashfreeGatewayService(cashfreeSettingsResolver, cashfreeClientFactory);
    }

    private CashfreeRuntimeConfig runtimeConfig() {
        return new CashfreeRuntimeConfig(
                true,
                "2023-08-01",
                "merchant-client-id",
                "merchant-client-secret",
                true
        );
    }
}

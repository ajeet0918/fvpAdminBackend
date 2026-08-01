package com.agriplatform.backend.payment.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.service.OrderService;
import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.repository.OrderPaymentEventRepository;
import com.agriplatform.backend.payment.model.OrderRefundStatus;
import com.cashfree.pg.Cashfree;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CashfreeWebhookServiceTest {
    private static final String TEST_PAYLOAD = """
            {
              "data": {"test_object": {"test_key": "test_value"}},
              "event_time": "2026-07-28T16:45:07.500Z",
              "type": "WEBHOOK"
            }
            """;
    private static final String SUCCESS_PAYLOAD = """
            {
              "data": {
                "order": {"order_id": "FVP-ORDER-1", "order_amount": 100, "order_currency": "INR"},
                "payment": {"cf_payment_id": "1453002795", "payment_status": "SUCCESS"}
              },
              "event_time": "2026-08-01T10:00:00+05:30",
              "type": "PAYMENT_SUCCESS_WEBHOOK"
            }
            """;
    private static final String REFUND_PAYLOAD = """
            {
              "data": {
                "refund": {
                  "cf_refund_id": 11325632,
                  "cf_payment_id": 789727431,
                  "refund_id": "REF-ORDER-1",
                  "order_id": "FVP-ORDER-1",
                  "refund_amount": 40.00,
                  "refund_currency": "INR",
                  "refund_status": "SUCCESS",
                  "refund_note": "Cancelled before dispatch",
                  "processed_speed": "STANDARD",
                  "processed_at": "2026-08-01T12:00:00+05:30"
                }
              },
              "event_time": "2026-08-01T12:00:01+05:30",
              "type": "REFUND_STATUS_WEBHOOK"
            }
            """;

    @Mock
    private CashfreeSettingsResolver cashfreeSettingsResolver;

    @Mock
    private CashfreeClientFactory cashfreeClientFactory;

    @Mock
    private Cashfree cashfree;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderPaymentEventRepository orderPaymentEventRepository;

    @Mock
    private OrderRefundService orderRefundService;

    @Test
    void processWebhookAcceptsSignedConnectivityTestWithoutUpdatingOrders() throws Exception {
        CashfreeRuntimeConfig config = runtimeConfig();
        when(cashfreeSettingsResolver.resolve()).thenReturn(config);
        when(cashfreeClientFactory.create(config)).thenReturn(cashfree);
        CashfreeWebhookService service = new CashfreeWebhookService(
                cashfreeSettingsResolver,
                cashfreeClientFactory,
                new ObjectMapper(),
                orderService,
                orderPaymentEventRepository,
                orderRefundService
        );

        service.processWebhook("signature", "timestamp", TEST_PAYLOAD);

        verify(cashfree).PGVerifyWebhookSignature("signature", TEST_PAYLOAD, "timestamp");
        verify(orderPaymentEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(orderService);
    }

    @Test
    void processWebhookMarksOrderPaidFromCashfreePaymentSuccessPayload() throws Exception {
        CashfreeRuntimeConfig config = runtimeConfig();
        PurchaseOrder order = mock(PurchaseOrder.class);
        when(cashfreeSettingsResolver.resolve()).thenReturn(config);
        when(cashfreeClientFactory.create(config)).thenReturn(cashfree);
        when(orderService.markPaymentSuccessByGatewayOrderReference("FVP-ORDER-1", "1453002795"))
                .thenReturn(order);
        CashfreeWebhookService service = new CashfreeWebhookService(
                cashfreeSettingsResolver,
                cashfreeClientFactory,
                new ObjectMapper(),
                orderService,
                orderPaymentEventRepository,
                orderRefundService
        );

        service.processWebhook("signature", "timestamp", SUCCESS_PAYLOAD);

        verify(orderService).markPaymentSuccessByGatewayOrderReference("FVP-ORDER-1", "1453002795");
        verify(orderPaymentEventRepository).save(any());
    }

    @Test
    void processWebhookReconcilesCashfreeRefundPayload() throws Exception {
        CashfreeRuntimeConfig config = runtimeConfig();
        when(cashfreeSettingsResolver.resolve()).thenReturn(config);
        when(cashfreeClientFactory.create(config)).thenReturn(cashfree);
        CashfreeWebhookService service = new CashfreeWebhookService(
                cashfreeSettingsResolver,
                cashfreeClientFactory,
                new ObjectMapper(),
                orderService,
                orderPaymentEventRepository,
                orderRefundService
        );

        service.processWebhook("signature", "timestamp", REFUND_PAYLOAD);

        verify(orderRefundService).reconcileWebhook(org.mockito.ArgumentMatchers.argThat(update ->
                update.orderNumber().equals("FVP-ORDER-1")
                        && update.refundId().equals("REF-ORDER-1")
                        && update.status() == OrderRefundStatus.SUCCESS
                        && update.amount().compareTo(new java.math.BigDecimal("40.00")) == 0
        ));
        verifyNoInteractions(orderService);
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

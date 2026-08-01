package com.agriplatform.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.order.model.OrderPaymentStatus;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrderRefundServiceTest {
    private PurchaseOrderRepository purchaseOrderRepository;
    private OrderRefundRepository orderRefundRepository;
    private CashfreeGatewayService cashfreeGatewayService;
    private OrderRefundService orderRefundService;

    @BeforeEach
    void setUp() {
        purchaseOrderRepository = mock(PurchaseOrderRepository.class);
        orderRefundRepository = mock(OrderRefundRepository.class);
        cashfreeGatewayService = mock(CashfreeGatewayService.class);
        orderRefundService = new OrderRefundService(
                purchaseOrderRepository,
                orderRefundRepository,
                cashfreeGatewayService
        );
    }

    @Test
    void createRefundSendsRemainingPaidAmountToCashfree() {
        PurchaseOrder order = paidOrder();
        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.saveAndFlush(order)).thenReturn(order);
        when(orderRefundRepository.save(any(OrderRefund.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cashfreeGatewayService.createRefund(any(), any(), any(), any(), any()))
                .thenReturn(new CashfreeRefundResult(
                        "cashfree-refund-id",
                        "cashfree-payment-id",
                        OrderRefundStatus.PENDING,
                        "Refund initiated",
                        null,
                        null
                ));

        OrderRefundResponse response = orderRefundService.createRefund(
                1L,
                new CreateOrderRefundRequest(new BigDecimal("40.00"), "Cancelled before dispatch", "STANDARD"),
                "stageadmin"
        );

        assertThat(response.amount()).isEqualByComparingTo("40.00");
        assertThat(response.status()).isEqualTo(OrderRefundStatus.PENDING);
        verify(cashfreeGatewayService).createRefund(
                any(PurchaseOrder.class),
                org.mockito.ArgumentMatchers.eq(new BigDecimal("40.00")),
                any(String.class),
                org.mockito.ArgumentMatchers.eq("Cancelled before dispatch"),
                org.mockito.ArgumentMatchers.eq("STANDARD")
        );
    }

    @Test
    void summarizeReportsPartialAndPendingRefundAmounts() {
        PurchaseOrder order = paidOrder();
        order.addRefund(refund(order, "REF-SUCCESS", "30.00", OrderRefundStatus.SUCCESS));
        order.addRefund(refund(order, "REF-PENDING", "20.00", OrderRefundStatus.PENDING));

        OrderRefundSummaryResponse summary = orderRefundService.summarize(order);

        assertThat(summary.status()).isEqualTo("PENDING");
        assertThat(summary.refundedAmount()).isEqualByComparingTo("30.00");
        assertThat(summary.pendingAmount()).isEqualByComparingTo("20.00");
        assertThat(summary.refundableAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void toResponsesReturnsNewestRefundFirst() {
        PurchaseOrder order = paidOrder();
        OrderRefund olderRefund = refund(order, "REF-OLDER", "10.00", OrderRefundStatus.SUCCESS);
        OrderRefund newerRefund = refund(order, "REF-NEWER", "15.00", OrderRefundStatus.PENDING);
        ReflectionTestUtils.setField(olderRefund, "createdAt", LocalDateTime.of(2026, 8, 1, 10, 0));
        ReflectionTestUtils.setField(newerRefund, "createdAt", LocalDateTime.of(2026, 8, 1, 11, 0));
        order.addRefund(olderRefund);
        order.addRefund(newerRefund);

        List<OrderRefundResponse> responses = orderRefundService.toResponses(order);

        assertThat(responses).extracting(OrderRefundResponse::refundId)
                .containsExactly("REF-NEWER", "REF-OLDER");
    }

    @Test
    void reconcileWebhookCreatesRefundInitiatedOutsideApplication() {
        PurchaseOrder order = paidOrder();
        RefundWebhookUpdate update = new RefundWebhookUpdate(
                "FVP-ORDER-1",
                "REF-EXTERNAL",
                "cashfree-refund-id",
                "cashfree-payment-id",
                new BigDecimal("100.00"),
                "INR",
                OrderRefundStatus.SUCCESS,
                "STANDARD",
                "Cashfree dashboard refund",
                "Refund processed successfully",
                "refund-arn",
                LocalDateTime.now()
        );
        when(purchaseOrderRepository.findByOrderNumberForUpdate("FVP-ORDER-1"))
                .thenReturn(Optional.of(order));
        when(orderRefundRepository.findByRefundId("REF-EXTERNAL")).thenReturn(Optional.empty());

        orderRefundService.reconcileWebhook(update);

        assertThat(order.getRefunds()).hasSize(1);
        assertThat(order.getRefunds().getFirst().getStatus()).isEqualTo(OrderRefundStatus.SUCCESS);
        verify(orderRefundRepository).save(order.getRefunds().getFirst());
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void syncRefundsImportsExistingCashfreeRefund() {
        PurchaseOrder order = paidOrder();
        CashfreeRefundSnapshot snapshot = new CashfreeRefundSnapshot(
                "REF-SYNCED",
                "cashfree-refund-id",
                "cashfree-payment-id",
                new BigDecimal("25.00"),
                "INR",
                OrderRefundStatus.SUCCESS,
                "STANDARD",
                "Refunded from Cashfree dashboard",
                "Refund processed",
                "refund-arn",
                LocalDateTime.now()
        );
        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));
        when(cashfreeGatewayService.fetchRefunds(order)).thenReturn(List.of(snapshot));
        when(orderRefundRepository.findByRefundId("REF-SYNCED")).thenReturn(Optional.empty());

        List<OrderRefundResponse> responses = orderRefundService.syncRefunds(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().status()).isEqualTo(OrderRefundStatus.SUCCESS);
        verify(cashfreeGatewayService).fetchRefunds(order);
    }

    private PurchaseOrder paidOrder() {
        PurchaseOrder order = mock(PurchaseOrder.class);
        List<OrderRefund> refunds = new ArrayList<>();
        when(order.getId()).thenReturn(1L);
        when(order.getOrderNumber()).thenReturn("FVP-ORDER-1");
        when(order.getCurrency()).thenReturn("INR");
        when(order.getPaymentStatus()).thenReturn(OrderPaymentStatus.PAID);
        when(order.getPaymentProvider()).thenReturn(CashfreeApiConstants.GATEWAY_NAME);
        when(order.getPaymentDueAmount()).thenReturn(new BigDecimal("100.00"));
        when(order.getRefunds()).thenReturn(refunds);
        org.mockito.Mockito.doAnswer(invocation -> refunds.add(invocation.getArgument(0)))
                .when(order).addRefund(any(OrderRefund.class));
        return order;
    }

    private OrderRefund refund(
            PurchaseOrder order,
            String refundId,
            String amount,
            OrderRefundStatus status
    ) {
        OrderRefund refund = new OrderRefund(
                order,
                refundId,
                new BigDecimal(amount),
                "INR",
                "STANDARD",
                "Test refund",
                "stageadmin"
        );
        refund.applyGatewayStatus(null, null, status, null, null, null);
        return refund;
    }
}

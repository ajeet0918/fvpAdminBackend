package com.agriplatform.backend.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.customer.repository.CustomerAddressRepository;
import com.agriplatform.backend.customer.repository.CustomerRepository;
import com.agriplatform.backend.order.model.OrderPaymentStatus;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.repository.PurchaseOrderRepository;
import com.agriplatform.backend.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServicePaymentTest {
    private PurchaseOrderRepository purchaseOrderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        purchaseOrderRepository = mock(PurchaseOrderRepository.class);
        orderService = new OrderService(
                purchaseOrderRepository,
                mock(ProductRepository.class),
                mock(CustomerRepository.class),
                mock(CustomerAddressRepository.class)
        );
    }

    @Test
    void markPaymentSuccessFindsOrderByMerchantOrderNumber() {
        PurchaseOrder order = order("FVP-ORDER-1");
        when(purchaseOrderRepository.findByOrderNumber("FVP-ORDER-1")).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(order)).thenReturn(order);

        PurchaseOrder updated = orderService.markPaymentSuccessByGatewayOrderReference(
                "FVP-ORDER-1",
                "1453002795"
        );

        assertThat(updated.getPaymentStatus()).isEqualTo(OrderPaymentStatus.PAID);
        verify(purchaseOrderRepository, never()).findByPaymentProviderOrderId("FVP-ORDER-1");
    }

    @Test
    void markPaymentSuccessFallsBackToCashfreeOrderId() {
        PurchaseOrder order = order("FVP-ORDER-2");
        order.markPaymentPending("CASHFREE", "2149460581", new BigDecimal("100.00"));
        when(purchaseOrderRepository.findByOrderNumber("2149460581")).thenReturn(Optional.empty());
        when(purchaseOrderRepository.findByPaymentProviderOrderId("2149460581")).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(order)).thenReturn(order);

        PurchaseOrder updated = orderService.markPaymentSuccessByGatewayOrderReference(
                "2149460581",
                "1453002796"
        );

        assertThat(updated.getPaymentStatus()).isEqualTo(OrderPaymentStatus.PAID);
    }

    @Test
    void markPaymentFailureDoesNotDowngradePaidOrder() {
        PurchaseOrder order = order("FVP-ORDER-3");
        order.markPaymentPaid("1453002797");
        when(purchaseOrderRepository.findByOrderNumber("FVP-ORDER-3")).thenReturn(Optional.of(order));

        PurchaseOrder updated = orderService.markPaymentFailedByGatewayOrderReference(
                "FVP-ORDER-3",
                "1453002798"
        );

        assertThat(updated.getPaymentStatus()).isEqualTo(OrderPaymentStatus.PAID);
        verify(purchaseOrderRepository, never()).save(order);
    }

    private PurchaseOrder order(String orderNumber) {
        return new PurchaseOrder(
                orderNumber,
                "Test Customer",
                "Test Company",
                "customer@example.com",
                "9999999999",
                "Test Address",
                "Noida",
                "Uttar Pradesh",
                "201301",
                "Test order"
        );
    }
}

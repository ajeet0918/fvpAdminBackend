package com.agriplatform.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.dto.CashfreeCreateOrderResult;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.model.OrderEntity;
import java.math.BigDecimal;
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

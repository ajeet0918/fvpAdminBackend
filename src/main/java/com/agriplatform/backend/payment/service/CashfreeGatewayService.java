package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.dto.CashfreeCreateOrderResult;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.model.CreateOrderRequest;
import com.cashfree.pg.model.CustomerDetails;
import com.cashfree.pg.model.OrderEntity;
import com.cashfree.pg.model.OrderMeta;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CashfreeGatewayService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashfreeGatewayService.class);

    private final CashfreeSettingsResolver cashfreeSettingsResolver;

    public CashfreeGatewayService(CashfreeSettingsResolver cashfreeSettingsResolver) {
        this.cashfreeSettingsResolver = cashfreeSettingsResolver;
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
            Cashfree cashfree = new Cashfree(
                    resolveEnvironment(config),
                    config.apiVersion(),
                    config.clientId(),
                    config.clientSecret(),
                    null,
                    null
            );

            CreateOrderRequest request = new CreateOrderRequest()
                    .orderId(order.getOrderNumber())
                    .orderCurrency(CashfreeApiConstants.CURRENCY_INR)
                    .orderAmount(amount)
                    .customerDetails(buildCustomer(customer))
                    .orderMeta(buildOrderMeta(config, checkoutSuccessUrl, checkoutFailureUrl));

            ApiResponse<OrderEntity> response = cashfree.PGCreateOrder(
                    request,
                    config.apiVersion(),
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

    private CustomerDetails buildCustomer(Customer customer) {
        return new CustomerDetails()
                .customerId(String.valueOf(customer.getId()))
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .customerPhone(customer.getPhone());
    }

    private OrderMeta buildOrderMeta(CashfreeRuntimeConfig config, String checkoutSuccessUrl, String checkoutFailureUrl) {
        String fallbackUrl = hasText(checkoutFailureUrl) ? checkoutFailureUrl : checkoutSuccessUrl;
        String returnUrl = hasText(checkoutSuccessUrl)
                ? checkoutSuccessUrl + buildQueryDelimiter(checkoutSuccessUrl) + "order_id={order_id}"
                : fallbackUrl;

        return new OrderMeta()
                .returnUrl(returnUrl);
    }

    private Cashfree.CFEnvironment resolveEnvironment(CashfreeRuntimeConfig config) {
        return Cashfree.CFEnvironment.PRODUCTION;
    }

    private String buildQueryDelimiter(String url) {
        return url.contains("?") ? "&" : "?";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

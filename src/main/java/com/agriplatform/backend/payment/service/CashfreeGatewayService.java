package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.payment.config.CashfreeProperties;
import com.agriplatform.backend.payment.dto.CashfreeCreateOrderResult;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.model.CreateOrderRequest;
import com.cashfree.pg.model.CustomerDetails;
import com.cashfree.pg.model.OrderEntity;
import com.cashfree.pg.model.OrderMeta;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CashfreeGatewayService {

    private final CashfreeProperties properties;

    public CashfreeGatewayService(CashfreeProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled()
                && hasText(properties.getClientId())
                && hasText(properties.getClientSecret());
    }

    public CashfreeCreateOrderResult createOrder(
            PurchaseOrder order,
            Customer customer,
            BigDecimal amount,
            String checkoutSuccessUrl,
            String checkoutFailureUrl
    ) {
        if (!isEnabled()) {
            throw new IllegalStateException("Cashfree gateway is disabled or not configured");
        }

        try {
            Cashfree cashfree = new Cashfree(
                    resolveEnvironment(),
                    properties.getApiVersion(),
                    properties.getClientId(),
                    properties.getClientSecret(),
                    null,
                    null
            );

            CreateOrderRequest request = new CreateOrderRequest()
                    .orderId(order.getOrderNumber())
                    .orderCurrency(CashfreeApiConstants.CURRENCY_INR)
                    .orderAmount(amount)
                    .customerDetails(buildCustomer(customer))
                    .orderMeta(buildOrderMeta(order, checkoutSuccessUrl, checkoutFailureUrl));

            ApiResponse<OrderEntity> response = cashfree.PGCreateOrder(
                    request,
                    properties.getApiVersion(),
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

    private OrderMeta buildOrderMeta(PurchaseOrder order, String checkoutSuccessUrl, String checkoutFailureUrl) {
        String fallbackUrl = hasText(checkoutFailureUrl) ? checkoutFailureUrl : checkoutSuccessUrl;
        String returnUrl = hasText(checkoutSuccessUrl)
                ? checkoutSuccessUrl + buildQueryDelimiter(checkoutSuccessUrl) + "order_id={order_id}"
                : fallbackUrl;

        return new OrderMeta()
                .returnUrl(returnUrl)
                .notifyUrl(resolveNotifyUrl());
    }

    private String resolveNotifyUrl() {
        if (hasText(properties.getWebhookNotifyUrl())) {
            return properties.getWebhookNotifyUrl();
        }
        return "";
    }

    private Cashfree.CFEnvironment resolveEnvironment() {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl != null && baseUrl.toLowerCase().contains("sandbox")) {
            return Cashfree.CFEnvironment.SANDBOX;
        }
        return Cashfree.CFEnvironment.PRODUCTION;
    }

    private String buildQueryDelimiter(String url) {
        return url.contains("?") ? "&" : "?";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

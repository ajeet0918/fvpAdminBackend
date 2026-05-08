package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.payment.config.CashfreeProperties;
import com.agriplatform.backend.payment.dto.CashfreeCreateOrderResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CashfreeGatewayService {

    private final CashfreeProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CashfreeGatewayService(CashfreeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public CashfreeCreateOrderResult createOrder(
            PurchaseOrder order,
            Customer customer,
            BigDecimal amount,
            String successUrl,
            String failureUrl
    ) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("Cashfree is not enabled");
        }
        if (isBlank(properties.getClientId()) || isBlank(properties.getClientSecret())) {
            throw new IllegalStateException("Cashfree credentials are not configured");
        }

        String customerPhone = customer.getPhone() == null || customer.getPhone().isBlank()
                ? "9999999999"
                : customer.getPhone().trim();
        String customerName = customer.getFullName() == null || customer.getFullName().isBlank()
                ? "FVP Customer"
                : customer.getFullName().trim();
        String customerEmail = customer.getEmail() == null || customer.getEmail().isBlank()
                ? "unknown@fvppurepick.com"
                : customer.getEmail().trim();

        String requestId = UUID.randomUUID().toString();
        String payload = buildCreateOrderPayload(
                order.getOrderNumber(),
                amount,
                customerName,
                customerEmail,
                customerPhone,
                successUrl,
                failureUrl
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(properties.getBaseUrl()) + CashfreeApiConstants.API_ORDERS_PATH))
                .header(CashfreeApiConstants.HEADER_CONTENT_TYPE, "application/json")
                .header(CashfreeApiConstants.HEADER_CLIENT_ID, properties.getClientId())
                .header(CashfreeApiConstants.HEADER_CLIENT_SECRET, properties.getClientSecret())
                .header(CashfreeApiConstants.HEADER_API_VERSION, defaultApiVersion())
                .header(CashfreeApiConstants.HEADER_REQUEST_ID, requestId)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to connect to Cashfree", ex);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Cashfree order creation failed: " + response.body());
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            String providerOrderId = text(root, CashfreeApiConstants.FIELD_CF_ORDER_ID);
            String paymentSessionId = text(root, CashfreeApiConstants.FIELD_PAYMENT_SESSION_ID);
            String paymentLink = text(root, CashfreeApiConstants.FIELD_PAYMENT_LINK);
            if (providerOrderId.isBlank() || paymentSessionId.isBlank()) {
                throw new IllegalStateException("Cashfree response missing order/session id");
            }
            return new CashfreeCreateOrderResult(providerOrderId, paymentSessionId, paymentLink);
        } catch (IOException ex) {
            throw new IllegalStateException("Invalid Cashfree response", ex);
        }
    }

    private String buildCreateOrderPayload(
            String orderId,
            BigDecimal amount,
            String customerName,
            String customerEmail,
        String customerPhone,
        String successUrl,
        String failureUrl
    ) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("order_id", orderId);
        root.put("order_amount", amount == null ? BigDecimal.ZERO : amount);
        root.put("order_currency", CashfreeApiConstants.CURRENCY_INR);

        ObjectNode customerDetails = root.putObject("customer_details");
        customerDetails.put("customer_id", orderId);
        customerDetails.put("customer_name", customerName);
        customerDetails.put("customer_email", customerEmail);
        customerDetails.put("customer_phone", customerPhone);

        ObjectNode orderMeta = root.putObject("order_meta");
        orderMeta.put("return_url", successUrl + "?order_id={order_id}&order_token={order_token}");
        orderMeta.put("notify_url", failureUrl);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to serialize Cashfree order payload", ex);
        }
    }

    private String defaultApiVersion() {
        return isBlank(properties.getApiVersion()) ? CashfreeApiConstants.DEFAULT_API_VERSION : properties.getApiVersion().trim();
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node == null ? "" : node.asText("").trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

}

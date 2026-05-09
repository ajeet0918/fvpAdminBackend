package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.service.OrderService;
import com.agriplatform.backend.payment.config.CashfreeProperties;
import com.agriplatform.backend.payment.model.OrderPaymentEvent;
import com.agriplatform.backend.payment.repository.OrderPaymentEventRepository;
import com.cashfree.pg.Cashfree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashfreeWebhookService {

    private final CashfreeProperties properties;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final OrderPaymentEventRepository orderPaymentEventRepository;

    public CashfreeWebhookService(
            CashfreeProperties properties,
            ObjectMapper objectMapper,
            OrderService orderService,
            OrderPaymentEventRepository orderPaymentEventRepository
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.orderPaymentEventRepository = orderPaymentEventRepository;
    }

    @Transactional
    public void processWebhook(String signature, String timestamp, String payload) {
        if (properties.isEnforceWebhookSignature()) {
            validateSignature(signature, timestamp, payload);
        }

        JsonNode root = parse(payload);
        String providerOrderId = firstText(root, CashfreeApiConstants.ORDER_ID_PATHS);
        String orderStatus = firstText(root, CashfreeApiConstants.ORDER_STATUS_PATHS);
        String eventType = firstText(root, CashfreeApiConstants.EVENT_TYPE_PATHS);
        String paymentReference = firstText(root, CashfreeApiConstants.PAYMENT_REFERENCE_PATHS);

        if (providerOrderId.isBlank()) {
            throw new IllegalArgumentException("Missing Cashfree order reference");
        }

        PurchaseOrder order;
        if (matches(orderStatus, CashfreeApiConstants.SUCCESS_STATUSES)) {
            order = orderService.markPaymentSuccessByProviderOrderId(providerOrderId, paymentReference);
        } else if (matches(orderStatus, CashfreeApiConstants.FAILURE_STATUSES)) {
            order = orderService.markPaymentFailedByProviderOrderId(providerOrderId, paymentReference);
        } else {
            order = orderService.findByPaymentProviderOrderId(providerOrderId);
        }

        orderPaymentEventRepository.save(new OrderPaymentEvent(
                order,
                eventType.isBlank() ? CashfreeApiConstants.DEFAULT_EVENT_TYPE : eventType,
                providerOrderId,
                paymentReference,
                orderStatus,
                truncate(payload, 2000)
        ));
    }

    private void validateSignature(String signature, String timestamp, String payload) {
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Missing webhook signature");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("Missing webhook timestamp");
        }
        String verificationSecret = resolveWebhookVerificationSecret();
        if (verificationSecret.isBlank()) {
            throw new IllegalArgumentException("Cashfree client secret is not configured");
        }

        try {
            Cashfree cashfree = new Cashfree(
                    resolveEnvironment(),
                    properties.getApiVersion(),
                    properties.getClientId(),
                    verificationSecret,
                    null,
                    null
            );
            cashfree.PGVerifyWebhookSignature(signature.trim(), payload, timestamp.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid webhook signature", ex);
        }
    }

    private Cashfree.CFEnvironment resolveEnvironment() {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl != null && baseUrl.toLowerCase().contains("sandbox")) {
            return Cashfree.CFEnvironment.SANDBOX;
        }
        return Cashfree.CFEnvironment.PRODUCTION;
    }

    private String resolveWebhookVerificationSecret() {
        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
            return properties.getClientSecret().trim();
        }
        return "";
    }

    private JsonNode parse(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid webhook payload", ex);
        }
    }

    private String firstText(JsonNode root, String... paths) {
        for (String path : paths) {
            JsonNode node = find(root, path);
            if (node != null && !node.isMissingNode()) {
                String value = node.asText("");
                if (!value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return "";
    }

    private JsonNode find(JsonNode root, String path) {
        JsonNode current = root;
        String[] parts = path.split("\\.");
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            current = current.get(part);
        }
        return current;
    }

    private boolean matches(String value, java.util.Set<String> expectedUpperCaseValues) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return expectedUpperCaseValues.contains(value.trim().toUpperCase());
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}

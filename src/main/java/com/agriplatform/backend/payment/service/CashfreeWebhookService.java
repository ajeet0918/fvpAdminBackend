package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.service.OrderService;
import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.model.OrderPaymentEvent;
import com.agriplatform.backend.payment.repository.OrderPaymentEventRepository;
import com.cashfree.pg.Cashfree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashfreeWebhookService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashfreeWebhookService.class);

    private final CashfreeSettingsResolver cashfreeSettingsResolver;
    private final CashfreeClientFactory cashfreeClientFactory;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final OrderPaymentEventRepository orderPaymentEventRepository;

    public CashfreeWebhookService(
            CashfreeSettingsResolver cashfreeSettingsResolver,
            CashfreeClientFactory cashfreeClientFactory,
            ObjectMapper objectMapper,
            OrderService orderService,
            OrderPaymentEventRepository orderPaymentEventRepository
    ) {
        this.cashfreeSettingsResolver = cashfreeSettingsResolver;
        this.cashfreeClientFactory = cashfreeClientFactory;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.orderPaymentEventRepository = orderPaymentEventRepository;
    }

    @Transactional
    public void processWebhook(String signature, String timestamp, String payload) {
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        if (config.enforceWebhookSignature()) {
            validateSignature(config, signature, timestamp, payload);
        }

        JsonNode root = parse(payload);
        if (isConnectivityTest(root)) {
            log.info("Cashfree webhook connectivity test accepted");
            return;
        }
        String merchantOrderId = firstText(root, CashfreeApiConstants.MERCHANT_ORDER_ID_PATHS);
        String providerOrderId = firstText(root, CashfreeApiConstants.PROVIDER_ORDER_ID_PATHS);
        String orderReference = hasText(merchantOrderId) ? merchantOrderId : providerOrderId;
        String paymentStatus = firstText(root, CashfreeApiConstants.PAYMENT_STATUS_PATHS);
        String eventType = firstText(root, CashfreeApiConstants.EVENT_TYPE_PATHS);
        String paymentReference = firstText(root, CashfreeApiConstants.PAYMENT_REFERENCE_PATHS);

        if (orderReference.isBlank()) {
            throw new IllegalArgumentException("Missing Cashfree order reference");
        }

        PurchaseOrder order;
        if (matches(paymentStatus, CashfreeApiConstants.SUCCESS_STATUSES)) {
            order = orderService.markPaymentSuccessByGatewayOrderReference(orderReference, paymentReference);
        } else if (matches(paymentStatus, CashfreeApiConstants.FAILURE_STATUSES)) {
            order = orderService.markPaymentFailedByGatewayOrderReference(orderReference, paymentReference);
        } else {
            order = orderService.findByGatewayOrderReference(orderReference);
        }

        orderPaymentEventRepository.save(new OrderPaymentEvent(
                order,
                eventType.isBlank() ? CashfreeApiConstants.DEFAULT_EVENT_TYPE : eventType,
                hasText(providerOrderId) ? providerOrderId : orderReference,
                paymentReference,
                paymentStatus,
                truncate(payload, 2000)
        ));
    }

    private void validateSignature(CashfreeRuntimeConfig config, String signature, String timestamp, String payload) {
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Missing webhook signature");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("Missing webhook timestamp");
        }
        if (!hasText(config.clientSecret())) {
            throw new IllegalArgumentException("Cashfree client secret is not configured");
        }

        try {
            Cashfree cashfree = cashfreeClientFactory.create(config);
            cashfree.PGVerifyWebhookSignature(signature.trim(), payload, timestamp.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid webhook signature", ex);
        }
    }

    private boolean isConnectivityTest(JsonNode root) {
        JsonNode testObject = find(root, "data.test_object");
        return "WEBHOOK".equalsIgnoreCase(firstText(root, CashfreeApiConstants.FIELD_TYPE))
                && testObject != null
                && testObject.isObject();
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

package com.agriplatform.backend.investor.service;

import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.dto.CashfreePaymentLinkWebhookPayload;
import com.agriplatform.backend.payment.service.CashfreeClientFactory;
import com.agriplatform.backend.payment.service.CashfreeSettingsResolver;
import com.cashfree.pg.Cashfree;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class InvestorPaymentWebhookService {
    private final CashfreeSettingsResolver cashfreeSettingsResolver;
    private final CashfreeClientFactory cashfreeClientFactory;
    private final ObjectMapper objectMapper;
    private final InvestorOnboardingService investorOnboardingService;

    public InvestorPaymentWebhookService(
            CashfreeSettingsResolver cashfreeSettingsResolver,
            CashfreeClientFactory cashfreeClientFactory,
            ObjectMapper objectMapper,
            InvestorOnboardingService investorOnboardingService
    ) {
        this.cashfreeSettingsResolver = cashfreeSettingsResolver;
        this.cashfreeClientFactory = cashfreeClientFactory;
        this.objectMapper = objectMapper;
        this.investorOnboardingService = investorOnboardingService;
    }

    public void process(String signature, String timestamp, String idempotencyKey, String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Cashfree payment-link webhook payload is empty");
        }
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        verifySignature(config, signature, timestamp, rawPayload);
        CashfreePaymentLinkWebhookPayload payload = parse(rawPayload);
        String eventKey = buildEventKey(idempotencyKey, payload);
        investorOnboardingService.processWebhook(payload, eventKey, serializeSanitized(payload));
    }

    private void verifySignature(
            CashfreeRuntimeConfig config,
            String signature,
            String timestamp,
            String rawPayload
    ) {
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Missing Cashfree webhook signature");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("Missing Cashfree webhook timestamp");
        }
        if (config.clientSecret() == null || config.clientSecret().isBlank()) {
            throw new IllegalArgumentException("Cashfree client secret is not configured");
        }
        try {
            Cashfree cashfree = cashfreeClientFactory.create(config);
            cashfree.PGVerifyWebhookSignature(signature.trim(), rawPayload, timestamp.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Cashfree webhook signature", ex);
        }
    }

    private CashfreePaymentLinkWebhookPayload parse(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, CashfreePaymentLinkWebhookPayload.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid Cashfree payment-link webhook payload", ex);
        }
    }

    private String serializeSanitized(CashfreePaymentLinkWebhookPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to create Cashfree payment-link event audit snapshot", ex);
        }
    }

    private String buildEventKey(String idempotencyKey, CashfreePaymentLinkWebhookPayload payload) {
        String source = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey.trim()
                : String.join("|",
                        text(payload.type()),
                        text(payload.eventTime()),
                        payload.data() == null ? "" : text(payload.data().merchantLinkId()),
                        payload.data() == null ? "" : text(payload.data().linkStatus()),
                        payload.data() == null ? "" : String.valueOf(payload.data().amountPaid()),
                        payload.data() == null || payload.data().order() == null
                                ? "" : text(payload.data().order().transactionId())
                );
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String text(String value) {
        return value == null ? "" : value;
    }
}

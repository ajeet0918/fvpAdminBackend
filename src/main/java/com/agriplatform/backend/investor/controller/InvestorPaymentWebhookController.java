package com.agriplatform.backend.investor.controller;

import com.agriplatform.backend.investor.service.InvestorPaymentWebhookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inquiries/investor/payment-links/cashfree")
public class InvestorPaymentWebhookController {
    private final InvestorPaymentWebhookService investorPaymentWebhookService;

    public InvestorPaymentWebhookController(InvestorPaymentWebhookService investorPaymentWebhookService) {
        this.investorPaymentWebhookService = investorPaymentWebhookService;
    }

    @PostMapping("/webhook")
    public void webhook(
            @RequestHeader(value = "x-webhook-signature", required = false) String webhookSignature,
            @RequestHeader(value = "x-webhook-timestamp", required = false) String webhookTimestamp,
            @RequestHeader(value = "x-cashfree-signature", required = false) String legacySignature,
            @RequestHeader(value = "x-cashfree-timestamp", required = false) String legacyTimestamp,
            @RequestHeader(value = "x-idempotency-key", required = false) String idempotencyKey,
            @RequestBody String rawPayload
    ) {
        investorPaymentWebhookService.process(
                firstText(webhookSignature, legacySignature),
                firstText(webhookTimestamp, legacyTimestamp),
                idempotencyKey,
                rawPayload
        );
    }

    private String firstText(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }
}

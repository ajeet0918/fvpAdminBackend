package com.agriplatform.backend.payment.controller;

import com.agriplatform.backend.payment.service.CashfreeWebhookService;
import com.agriplatform.backend.payment.service.CashfreeApiConstants;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/cashfree")
public class CashfreeWebhookController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashfreeWebhookController.class);

    private final CashfreeWebhookService cashfreeWebhookService;

    public CashfreeWebhookController(CashfreeWebhookService cashfreeWebhookService) {
        this.cashfreeWebhookService = cashfreeWebhookService;
    }

    @PostMapping("/webhook")
    public void webhook(
            @RequestHeader(value = CashfreeApiConstants.HEADER_WEBHOOK_SIGNATURE, required = false) String signature,
            @RequestHeader(value = CashfreeApiConstants.HEADER_WEBHOOK_TIMESTAMP, required = false) String timestamp,
            @RequestBody String payload
    ) {
        cashfreeWebhookService.processWebhook(signature, timestamp, payload);
    }
}

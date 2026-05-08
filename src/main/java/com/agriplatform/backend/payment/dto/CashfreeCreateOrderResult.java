package com.agriplatform.backend.payment.dto;

public record CashfreeCreateOrderResult(
        String providerOrderId,
        String paymentSessionId,
        String paymentLink
) {
}

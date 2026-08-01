package com.agriplatform.backend.payment.dto;

public record CashfreeOrderStatusResult(
        String providerOrderId,
        String paymentSessionId,
        String orderStatus,
        String paymentReference
) {
}

package com.agriplatform.backend.order.dto;

public record OrderPaymentSessionResponse(
        Long orderId,
        String orderNumber,
        String paymentProvider,
        String providerOrderId,
        String paymentSessionId,
        String paymentLink,
        String message
) {
}

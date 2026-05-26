package com.agriplatform.backend.payment.config;

public record CashfreeRuntimeConfig(
        boolean enabled,
        String apiVersion,
        String clientId,
        String clientSecret,
        boolean enforceWebhookSignature
) {
    public boolean hasCredentials() {
        return hasText(clientId) && hasText(clientSecret);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

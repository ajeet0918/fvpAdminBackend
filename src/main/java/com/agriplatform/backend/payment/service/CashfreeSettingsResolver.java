package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.payment.config.CashfreeProperties;
import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.settings.service.AppSettingService;
import org.springframework.stereotype.Service;

@Service
public class CashfreeSettingsResolver {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashfreeSettingsResolver.class);

    private static final String KEY_ENABLED = "payment.cashfree.enabled";
    private static final String KEY_API_VERSION = "payment.cashfree.api-version";
    private static final String KEY_CLIENT_ID = "payment.cashfree.client-id";
    private static final String KEY_CLIENT_SECRET = "payment.cashfree.client-secret";
    private static final String KEY_WEBHOOK_ENFORCE_SIGNATURE = "payment.cashfree.webhook-enforce-signature";

    private final AppSettingService appSettingService;
    private final CashfreeProperties cashfreeProperties;

    public CashfreeSettingsResolver(AppSettingService appSettingService, CashfreeProperties cashfreeProperties) {
        this.appSettingService = appSettingService;
        this.cashfreeProperties = cashfreeProperties;
    }

    public CashfreeRuntimeConfig resolve() {
        boolean enabled = resolveBoolean(KEY_ENABLED, cashfreeProperties.resolveEnabled());
        String apiVersion = resolveString(KEY_API_VERSION, cashfreeProperties.resolveApiVersion());
        String clientId = resolveString(KEY_CLIENT_ID, cashfreeProperties.resolveClientId());
        String clientSecret = resolveString(KEY_CLIENT_SECRET, cashfreeProperties.resolveClientSecret());
        boolean enforceWebhookSignature = resolveBoolean(
                KEY_WEBHOOK_ENFORCE_SIGNATURE,
                cashfreeProperties.isEnforceWebhookSignature()
        );

        return new CashfreeRuntimeConfig(
                enabled,
                apiVersion,
                clientId,
                clientSecret,
                enforceWebhookSignature
        );
    }

    private String resolveString(String key, String fallback) {
        String value = appSettingService.getActiveValue(key);
        if (hasText(value)) {
            return value.trim();
        }
        return fallback == null ? "" : fallback.trim();
    }

    private boolean resolveBoolean(String key, boolean fallback) {
        String value = appSettingService.getActiveValue(key);
        if (!hasText(value)) {
            return fallback;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

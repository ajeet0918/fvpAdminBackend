package com.agriplatform.backend.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cashfree")
public class CashfreeProperties {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashfreeProperties.class);

    private boolean enabled;
    private String baseUrl;
    private String apiVersion;
    private String clientId;
    private String clientSecret;
    private boolean enforceWebhookSignature;
    private String webhookNotifyUrl;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isEnforceWebhookSignature() {
        return enforceWebhookSignature;
    }

    public void setEnforceWebhookSignature(boolean enforceWebhookSignature) {
        this.enforceWebhookSignature = enforceWebhookSignature;
    }

    public String getWebhookNotifyUrl() {
        return webhookNotifyUrl;
    }

    public void setWebhookNotifyUrl(String webhookNotifyUrl) {
        this.webhookNotifyUrl = webhookNotifyUrl;
    }

    // Resolution policy: application properties first, environment variables second.
    public boolean resolveEnabled() {
        String env = System.getenv("CASHFREE_ENABLED");
        if (env == null || env.isBlank()) {
            return enabled;
        }
        if (enabled) {
            return true;
        }
        return Boolean.parseBoolean(env.trim());
    }

    public String resolveBaseUrl() {
        return firstNonBlank(baseUrl, System.getenv("CASHFREE_BASE_URL"));
    }

    public String resolveApiVersion() {
        return firstNonBlank(apiVersion, System.getenv("CASHFREE_API_VERSION"));
    }

    public String resolveClientId() {
        return firstNonBlank(clientId, System.getenv("CASHFREE_CLIENT_ID"));
    }

    public String resolveClientSecret() {
        return firstNonBlank(clientSecret, System.getenv("CASHFREE_CLIENT_SECRET"));
    }

    public String resolveWebhookNotifyUrl() {
        return firstNonBlank(webhookNotifyUrl, System.getenv("CASHFREE_WEBHOOK_NOTIFY_URL"));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return "";
    }
}

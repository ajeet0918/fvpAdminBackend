package com.agriplatform.backend.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cashfree")
public class CashfreeProperties {

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
}

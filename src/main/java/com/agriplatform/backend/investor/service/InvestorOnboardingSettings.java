package com.agriplatform.backend.investor.service;

import com.agriplatform.backend.settings.model.SmtpConfig;
import com.agriplatform.backend.settings.service.AppSettingService;
import com.agriplatform.backend.settings.service.SmtpConfigService;
import java.net.URI;
import org.springframework.stereotype.Service;

@Service
public class InvestorOnboardingSettings {
    private static final String COMPANY_NAME = "investor.company.legal-name";
    private static final String COMPANY_ADDRESS = "investor.company.address";
    private static final String AUTHORIZED_SIGNATORY = "investor.company.authorized-signatory";
    private static final String TERMS_VERSION = "investor.agreement.terms-version";
    private static final String TERMS_TEXT = "investor.agreement.terms-text";
    private static final String LINK_EXPIRY_DAYS = "investor.payment-link.expiry-days";
    private static final String WEBHOOK_URL = "payment.cashfree.webhook-notify-url";
    private static final String RETURN_URL = "investor.payment-link.return-url";
    private static final String SUPPORT_EMAIL = "general.support-email";

    private final AppSettingService appSettingService;
    private final SmtpConfigService smtpConfigService;

    public InvestorOnboardingSettings(
            AppSettingService appSettingService,
            SmtpConfigService smtpConfigService
    ) {
        this.appSettingService = appSettingService;
        this.smtpConfigService = smtpConfigService;
    }

    public Snapshot load() {
        SmtpConfig smtp = smtpConfigService.getActiveConfig()
                .orElseThrow(() -> new IllegalArgumentException("SMTP is not configured or not active"));
        requireHttpsUrl(smtp.getFrontendBaseUrl(), "SMTP frontend base URL");
        String webhookUrl = requireHttpsUrl(requireSetting(WEBHOOK_URL), "Cashfree webhook notify URL");
        String returnUrl = requireHttpsUrl(requireSetting(RETURN_URL), "Investor payment return URL");
        int expiryDays = parseExpiryDays(requireSetting(LINK_EXPIRY_DAYS));

        return new Snapshot(
                requireSetting(COMPANY_NAME),
                requireSetting(COMPANY_ADDRESS),
                requireSetting(AUTHORIZED_SIGNATORY),
                requireSetting(TERMS_VERSION),
                requireSetting(TERMS_TEXT),
                expiryDays,
                webhookUrl,
                returnUrl,
                optionalSetting(SUPPORT_EMAIL, smtp.getFromEmail())
        );
    }

    private String requireSetting(String key) {
        String value = appSettingService.getActiveValue(key);
        if (!hasText(value)) {
            throw new IllegalArgumentException("Required admin setting is missing: " + key);
        }
        return value.trim();
    }

    private String optionalSetting(String key, String fallback) {
        String value = appSettingService.getActiveValue(key);
        return hasText(value) ? value.trim() : fallback;
    }

    private int parseExpiryDays(String value) {
        try {
            int days = Integer.parseInt(value);
            if (days < 1 || days > 90) {
                throw new IllegalArgumentException("Investor payment-link expiry must be between 1 and 90 days");
            }
            return days;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Investor payment-link expiry must be a whole number", ex);
        }
    }

    private String requireHttpsUrl(String value, String label) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(label + " is required");
        }
        try {
            URI uri = URI.create(value.trim());
            if (uri.getHost() == null || !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException(label + " must be an absolute HTTPS URL");
            }
            return value.trim().replaceAll("/+$", "");
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(label + " is invalid", ex);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record Snapshot(
            String companyLegalName,
            String companyAddress,
            String authorizedSignatory,
            String termsVersion,
            String termsText,
            int paymentLinkExpiryDays,
            String webhookUrl,
            String paymentReturnUrl,
            String supportEmail
    ) {
    }
}

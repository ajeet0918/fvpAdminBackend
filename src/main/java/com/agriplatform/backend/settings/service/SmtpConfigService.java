package com.agriplatform.backend.settings.service;

import com.agriplatform.backend.settings.dto.SaveSmtpConfigRequest;
import com.agriplatform.backend.settings.dto.SmtpConfigResponse;
import com.agriplatform.backend.settings.model.SmtpConfig;
import com.agriplatform.backend.settings.repository.SmtpConfigRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmtpConfigService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SmtpConfigService.class);
    private static final String SECRET_MASK = "********";
    private static final Integer DEFAULT_SMTP_PORT = 587;

    private final SmtpConfigRepository smtpConfigRepository;

    public SmtpConfigService(SmtpConfigRepository smtpConfigRepository) {
        this.smtpConfigRepository = smtpConfigRepository;
    }

    @Transactional(readOnly = true)
    public SmtpConfigResponse getConfig() {
        return smtpConfigRepository.findFirstByOrderByUpdatedAtDesc()
                .map(config -> SmtpConfigResponse.from(config, resolveDisplayPassword(config)))
                .orElseGet(SmtpConfigResponse::empty);
    }

    @Transactional(readOnly = true)
    public Optional<SmtpConfig> getActiveConfig() {
        return smtpConfigRepository.findFirstByOrderByUpdatedAtDesc()
                .filter(SmtpConfig::isActive);
    }

    @Transactional
    public SmtpConfigResponse saveConfig(SaveSmtpConfigRequest request, String updatedBy) {
        SmtpConfig config = smtpConfigRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseGet(() -> new SmtpConfig(
                        false,
                        "",
                        DEFAULT_SMTP_PORT,
                        "",
                        "",
                        "",
                        "FVP Purepick",
                        true,
                        true,
                        "",
                        updatedBy
                ));

        validateRequiredFields(request, config);
        Integer resolvedPort = request.port() == null ? DEFAULT_SMTP_PORT : request.port();
        config.update(
                request.active(),
                normalizeText(request.host()),
                resolvedPort,
                normalizeText(request.username()),
                resolveNextPassword(config, request.password()),
                normalizeText(request.fromEmail()),
                normalizeText(request.fromName()),
                request.authEnabled(),
                request.startTlsEnabled(),
                normalizeText(request.frontendBaseUrl()),
                updatedBy
        );
        SmtpConfig saved = smtpConfigRepository.save(config);
        return SmtpConfigResponse.from(saved, resolveDisplayPassword(saved));
    }

    private void validateRequiredFields(SaveSmtpConfigRequest request, SmtpConfig existing) {
        if (!request.active()) {
            return;
        }
        if (!hasText(request.host())) {
            throw new IllegalArgumentException("SMTP host is required when SMTP is active");
        }
        if (request.port() == null) {
            throw new IllegalArgumentException("SMTP port is required when SMTP is active");
        }
        if (!hasText(request.fromEmail())) {
            throw new IllegalArgumentException("SMTP from email is required when SMTP is active");
        }
        if (request.authEnabled() && !hasText(request.username())) {
            throw new IllegalArgumentException("SMTP username is required when auth is enabled");
        }
        if (request.authEnabled() && !hasUsableSecret(request.password()) && !hasText(existing.getPassword())) {
            throw new IllegalArgumentException("SMTP password is required when auth is enabled");
        }
    }

    private String resolveNextPassword(SmtpConfig existing, String requestedPassword) {
        String normalizedPassword = normalizeText(requestedPassword);
        if (!hasText(normalizedPassword) || SECRET_MASK.equals(normalizedPassword)) {
            return existing.getPassword();
        }
        return normalizedPassword;
    }

    private String resolveDisplayPassword(SmtpConfig config) {
        return hasText(config.getPassword()) ? SECRET_MASK : "";
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasUsableSecret(String value) {
        return hasText(value) && !SECRET_MASK.equals(value.trim());
    }
}

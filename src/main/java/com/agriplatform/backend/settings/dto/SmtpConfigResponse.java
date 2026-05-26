package com.agriplatform.backend.settings.dto;

import com.agriplatform.backend.settings.model.SmtpConfig;
import java.time.LocalDateTime;

public record SmtpConfigResponse(
        Long id,
        boolean active,
        String host,
        Integer port,
        String username,
        String password,
        String fromEmail,
        String fromName,
        boolean authEnabled,
        boolean startTlsEnabled,
        String frontendBaseUrl,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SmtpConfigResponse empty() {
        return new SmtpConfigResponse(
                null,
                false,
                "",
                587,
                "",
                "",
                "",
                "FVP Purepick",
                true,
                true,
                "",
                null,
                null,
                null
        );
    }

    public static SmtpConfigResponse from(SmtpConfig config, String passwordValue) {
        return new SmtpConfigResponse(
                config.getId(),
                config.isActive(),
                config.getHost(),
                config.getPort(),
                config.getUsername(),
                passwordValue,
                config.getFromEmail(),
                config.getFromName(),
                config.isAuthEnabled(),
                config.isStartTlsEnabled(),
                config.getFrontendBaseUrl(),
                config.getUpdatedBy(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}

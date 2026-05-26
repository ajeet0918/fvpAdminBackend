package com.agriplatform.backend.settings.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SaveSmtpConfigRequest(
        boolean active,
        String host,
        @Min(1)
        @Max(65535)
        Integer port,
        String username,
        String password,
        @Email
        String fromEmail,
        String fromName,
        boolean authEnabled,
        boolean startTlsEnabled,
        String frontendBaseUrl
) {
}

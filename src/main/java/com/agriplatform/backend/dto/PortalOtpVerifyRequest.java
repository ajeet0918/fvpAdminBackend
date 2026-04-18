package com.agriplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortalOtpVerifyRequest(
        @NotBlank @Size(max = 255) String identifier,
        @NotBlank @Size(min = 4, max = 10) String otp
) {
}

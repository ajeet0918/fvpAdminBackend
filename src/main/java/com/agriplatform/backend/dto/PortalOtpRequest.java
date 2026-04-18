package com.agriplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortalOtpRequest(
        @NotBlank @Size(max = 255) String identifier
) {
}

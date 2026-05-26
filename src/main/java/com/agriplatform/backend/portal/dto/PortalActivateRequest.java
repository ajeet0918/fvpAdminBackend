package com.agriplatform.backend.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortalActivateRequest(
        @NotBlank
        String token,

        @NotBlank
        @Size(min = 8, max = 120)
        String password
) {
}

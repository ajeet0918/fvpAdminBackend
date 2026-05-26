package com.agriplatform.backend.portal.dto;

import jakarta.validation.constraints.NotBlank;

public record PortalPasswordResetRequest(
        @NotBlank
        String identifier
) {
}

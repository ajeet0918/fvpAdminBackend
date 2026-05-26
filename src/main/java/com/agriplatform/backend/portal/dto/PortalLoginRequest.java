package com.agriplatform.backend.portal.dto;

import jakarta.validation.constraints.NotBlank;

public record PortalLoginRequest(
        @NotBlank
        String username,

        @NotBlank
        String password
) {
}

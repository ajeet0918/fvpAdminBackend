package com.agriplatform.backend.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerGoogleAuthRequest(
        @NotBlank String idToken
) {
}

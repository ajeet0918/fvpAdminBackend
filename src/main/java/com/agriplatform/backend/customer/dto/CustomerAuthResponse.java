package com.agriplatform.backend.customer.dto;

public record CustomerAuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String role,
        CustomerProfileResponse profile
) {
}

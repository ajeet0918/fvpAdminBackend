package com.agriplatform.backend.dto;

public record PortalOtpVerifyResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String role
) {
}

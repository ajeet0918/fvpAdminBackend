package com.agriplatform.backend.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresInSeconds,
        String role
) {
}

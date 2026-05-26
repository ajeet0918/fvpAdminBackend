package com.agriplatform.backend.portal.dto;

public record PortalAuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String role,
        String username,
        String userType
) {
}

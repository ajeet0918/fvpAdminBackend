package com.agriplatform.backend.dto;

public record PortalOtpRequestResponse(
        String message,
        long expiresInSeconds,
        String devOtp
) {
}

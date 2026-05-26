package com.agriplatform.backend.portal.dto;

public record PortalAccountInviteResponse(
        Long portalUserId,
        String username,
        String email,
        String userType,
        String status,
        String message
) {
}

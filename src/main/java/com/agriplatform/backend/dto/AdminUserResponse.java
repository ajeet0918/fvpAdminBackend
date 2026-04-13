package com.agriplatform.backend.dto;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        boolean active,
        String status,
        LocalDateTime createdAt,
        String roleCode,
        String roleName
) {
}

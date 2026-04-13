package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAdminUserRequest(
        @NotBlank @Size(min = 2, max = 80) String firstName,
        @NotBlank @Size(min = 1, max = 80) String lastName,
        @Email @NotBlank @Size(min = 5, max = 255) String email,
        @Size(max = 40) String phone,
        @NotBlank @Size(min = 3, max = 50) String roleCode,
        boolean active
) {
}

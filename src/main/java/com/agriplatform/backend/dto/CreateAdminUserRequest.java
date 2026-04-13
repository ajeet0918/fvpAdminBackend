package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdminUserRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 2, max = 80) String firstName,
        @NotBlank @Size(min = 1, max = 80) String lastName,
        @Email @NotBlank @Size(min = 5, max = 255) String email,
        @Size(max = 40) String phone,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(min = 3, max = 50) String roleCode,
        boolean active
) {
}

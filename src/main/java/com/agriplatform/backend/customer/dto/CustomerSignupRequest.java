package com.agriplatform.backend.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerSignupRequest(
        @NotBlank @Size(max = 160) String fullName,
        @Size(max = 180) String companyName,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^[0-9+\\-\\s]{8,20}$") String phone,
        @NotBlank @Size(min = 8, max = 120) String password
) {
}

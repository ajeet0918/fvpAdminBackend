package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LeadRequest(
        @NotBlank @Size(max = 160) String fullName,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(max = 40) String phone,
        @Size(max = 255) String companyName,
        @Size(max = 1200) String notes
) {
}

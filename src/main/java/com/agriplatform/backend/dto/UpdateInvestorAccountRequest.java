package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateInvestorAccountRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 40) String phone,
        @NotBlank @Size(max = 30) String status,
        @NotBlank @Size(max = 20) String verificationStatus,
        @Size(max = 1200) String notes
) {
}

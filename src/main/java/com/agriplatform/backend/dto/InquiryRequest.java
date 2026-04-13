package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Size(max = 255) String companyName,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(max = 40) String phone,
        @NotBlank @Size(max = 255) String productName,
        @NotBlank @Size(min = 10, max = 1200) String message
) {
}

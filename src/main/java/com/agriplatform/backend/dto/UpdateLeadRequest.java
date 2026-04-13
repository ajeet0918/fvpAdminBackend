package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLeadRequest(
        @NotBlank @Size(max = 160) String fullName,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(max = 40) String phone,
        @Size(max = 255) String companyName,
        @NotBlank @Size(max = 40) String status,
        @Size(max = 80) String source,
        @Size(max = 1200) String notes,
        @Size(max = 120) String assignedTo,
        Long inquiryId
) {
}

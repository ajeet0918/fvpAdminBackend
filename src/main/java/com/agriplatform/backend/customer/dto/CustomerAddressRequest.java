package com.agriplatform.backend.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerAddressRequest(
        @NotBlank @Size(max = 60) String label,
        @NotBlank @Size(max = 160) String recipientName,
        @NotBlank @Size(max = 40) String phone,
        @NotBlank @Size(max = 400) String line1,
        @Size(max = 400) String line2,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 120) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Size(max = 120) String country,
        boolean isDefault
) {
}

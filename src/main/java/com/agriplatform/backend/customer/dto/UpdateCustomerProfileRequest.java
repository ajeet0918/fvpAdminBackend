package com.agriplatform.backend.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerProfileRequest(
        @NotBlank @Size(max = 160) String fullName,
        @Size(max = 180) String companyName,
        @NotBlank @Size(max = 255) String phone,
        @NotBlank @Size(max = 400) String deliveryAddress,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 120) String state,
        @NotBlank @Size(max = 20) String postalCode
) {
}

package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Size(max = 180) String companyName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 40) String phone,
        @NotBlank @Size(max = 400) String deliveryAddress,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 120) String state,
        @NotBlank @Size(max = 20) String postalCode,
        boolean active
) {
}

package com.agriplatform.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CollectionHubInquiryRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Size(max = 120) String fatherName,
        @NotBlank @Size(max = 40) String mobileNumber,
        @Size(max = 40) String alternateNumber,
        @NotBlank @Size(max = 40) String aadhaarNumber,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 500) String address,
        @NotBlank @Size(max = 160) String village,
        @NotBlank @Size(max = 160) String district,
        @NotBlank @Size(max = 120) String state,
        @NotBlank @Size(max = 20) String pinCode,
        @NotBlank @Size(max = 160) String collectionHubName,
        @NotBlank @Size(max = 80) String storageType,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal capacityMt,
        @NotNull Integer pickupRadiusKm,
        @NotBlank @Size(max = 120) String operatingDays,
        @Size(max = 1200) String notes,
        @NotNull Boolean termsAccepted
) {
}

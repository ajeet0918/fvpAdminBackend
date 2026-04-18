package com.agriplatform.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record InvestorInquiryRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Size(max = 120) String fatherName,
        @NotBlank @Size(max = 40) String mobileNumber,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 40) String aadhaarNumber,
        @NotBlank @Size(max = 20) String panNumber,
        @NotBlank @Size(max = 500) String fullAddress,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal investmentAmount,
        @NotBlank @Size(max = 20) String investmentDate,
        @Size(max = 80) String preferredPaymentMode,
        @Size(max = 120) String transactionId,
        @Size(max = 20) String paymentDate,
        @Size(max = 1200) String notes,
        @NotNull Boolean termsAccepted
) {
}

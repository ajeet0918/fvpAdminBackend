package com.agriplatform.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestorProfileUpsertRequest(
        @Size(max = 40) String investorCode,
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 40) String phone,
        Long sourceInquiryId,
        @NotBlank @Size(max = 30) String status,
        @NotBlank @Size(max = 20) String verificationStatus,
        @Size(max = 1200) String notes,
        Long investmentId,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal principalAmount,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal monthlyReturnRate,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 20) String investmentStatus,
        @Size(max = 1200) String investmentNotes
) {
}

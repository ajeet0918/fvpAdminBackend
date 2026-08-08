package com.agriplatform.backend.investor.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ApproveInvestorOnboardingRequest(
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "100.00") BigDecimal monthlyReturnRate,
        @NotNull LocalDate investmentStartDate,
        LocalDate investmentEndDate,
        @Size(max = 1200) String notes
) {
}

package com.agriplatform.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInvestmentRequest(
        @NotNull Long investorId,
        @Size(max = 40) String investmentReference,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal principalAmount,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal monthlyReturnRate,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotBlank @Size(max = 20) String status,
        @Size(max = 1200) String notes
) {
}

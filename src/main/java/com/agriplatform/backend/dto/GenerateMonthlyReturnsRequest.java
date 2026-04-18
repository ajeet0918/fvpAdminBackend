package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record GenerateMonthlyReturnsRequest(
        Long investorId,
        @NotNull Integer year,
        @NotNull @Min(1) @Max(12) Integer month,
        String distributionMode,
        BigDecimal monthlyRate,
        BigDecimal distributableProfit,
        BigDecimal companyFund,
        BigDecimal companyProfit,
        BigDecimal returnPercentage
) {
}

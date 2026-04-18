package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvestmentResponse(
        Long id,
        Long investorId,
        String investorCode,
        String investorName,
        String investmentReference,
        BigDecimal principalAmount,
        BigDecimal monthlyReturnRate,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

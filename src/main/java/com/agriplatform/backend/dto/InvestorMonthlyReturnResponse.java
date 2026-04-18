package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestorMonthlyReturnResponse(
        Long id,
        Long investorId,
        String investorCode,
        String investorName,
        Long investmentId,
        String investmentReference,
        Integer periodYear,
        Integer periodMonth,
        BigDecimal basePrincipal,
        BigDecimal returnRate,
        BigDecimal calculatedAmount,
        BigDecimal overrideAmount,
        BigDecimal finalAmount,
        String overrideReason,
        String status,
        Long payoutId,
        String payoutReference,
        String submittedBy,
        LocalDateTime submittedAt,
        String approvedBy,
        LocalDateTime approvedAt,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

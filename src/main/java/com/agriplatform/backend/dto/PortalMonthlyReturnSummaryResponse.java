package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortalMonthlyReturnSummaryResponse(
        Long id,
        Integer periodYear,
        Integer periodMonth,
        String investmentReference,
        BigDecimal basePrincipal,
        BigDecimal returnRate,
        BigDecimal calculatedAmount,
        BigDecimal overrideAmount,
        BigDecimal finalAmount,
        String status,
        String overrideReason,
        String payoutReference,
        String receiptNumber,
        LocalDateTime updatedAt
) {
}

package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortalInvestorSummaryResponse(
        Long id,
        String investorCode,
        String status,
        String verificationStatus,
        BigDecimal totalInvested,
        BigDecimal totalReturnsReceived,
        BigDecimal pendingPayout,
        LocalDateTime createdAt
) {
}

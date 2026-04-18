package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortalSummaryResponse(
        String identifier,
        BigDecimal totalInvested,
        BigDecimal totalCommittedReturn,
        BigDecimal totalReturnsReceived,
        BigDecimal pendingPayout,
        int orderCount,
        List<PortalOrderSummaryResponse> orders,
        List<PortalInvestorSummaryResponse> investors,
        List<PortalFarmerSummaryResponse> farmers,
        List<PortalMonthlyReturnSummaryResponse> monthlyReturns,
        List<PortalPayoutSummaryResponse> payouts
) {
}

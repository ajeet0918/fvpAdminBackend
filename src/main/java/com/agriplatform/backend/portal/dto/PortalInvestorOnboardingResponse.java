package com.agriplatform.backend.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PortalInvestorOnboardingResponse(
        String investorCode,
        String investorStatus,
        String investmentReference,
        String investmentStatus,
        BigDecimal principalAmount,
        BigDecimal monthlyReturnRate,
        String paymentStatus,
        BigDecimal amountPaid,
        LocalDateTime paidAt,
        List<AgreementSummary> agreements
) {
    public record AgreementSummary(
            Long id,
            String agreementNumber,
            String status,
            LocalDateTime generatedAt,
            String downloadUrl
    ) {
    }
}

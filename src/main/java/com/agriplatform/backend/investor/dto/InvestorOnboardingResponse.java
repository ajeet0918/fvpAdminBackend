package com.agriplatform.backend.investor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvestorOnboardingResponse(
        Long inquiryId,
        Long investorId,
        String investorCode,
        String investorStatus,
        String verificationStatus,
        Long investmentId,
        String investmentReference,
        String investmentStatus,
        BigDecimal principalAmount,
        BigDecimal monthlyReturnRate,
        LocalDate investmentStartDate,
        LocalDate investmentEndDate,
        Long paymentId,
        String merchantLinkId,
        String paymentLink,
        String paymentStatus,
        BigDecimal amountPaid,
        LocalDateTime paymentLinkExpiresAt,
        String paymentEmailStatus,
        LocalDateTime paymentEmailSentAt,
        String paymentEmailError,
        String portalInviteStatus,
        LocalDateTime portalInviteSentAt,
        String portalInviteError,
        Long agreementId,
        String agreementNumber,
        String agreementStatus,
        String agreementDownloadUrl,
        LocalDateTime agreementGeneratedAt,
        String agreementGenerationError
) {
}

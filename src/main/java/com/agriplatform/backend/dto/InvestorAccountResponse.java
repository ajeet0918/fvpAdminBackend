package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestorAccountResponse(
        Long id,
        String investorCode,
        String fullName,
        String email,
        String phone,
        Long sourceInquiryId,
        String status,
        String verificationStatus,
        String notes,
        BigDecimal totalInvested,
        BigDecimal totalReturnsReceived,
        BigDecimal pendingPayout,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

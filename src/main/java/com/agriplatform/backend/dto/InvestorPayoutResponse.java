package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvestorPayoutResponse(
        Long id,
        Long investorId,
        String investorCode,
        String investorName,
        String payoutReference,
        BigDecimal totalAmount,
        String status,
        String paymentChannel,
        String transactionReference,
        String notes,
        String approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime paidAt,
        Long receiptId,
        String receiptNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<Long> monthlyReturnIds
) {
}

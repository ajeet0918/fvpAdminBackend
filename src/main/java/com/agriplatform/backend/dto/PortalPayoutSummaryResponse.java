package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortalPayoutSummaryResponse(
        Long id,
        String payoutReference,
        BigDecimal totalAmount,
        String status,
        String paymentChannel,
        String transactionReference,
        LocalDateTime paidAt,
        String receiptNumber,
        Long receiptId,
        LocalDateTime createdAt
) {
}

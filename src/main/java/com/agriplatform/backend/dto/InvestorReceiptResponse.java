package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestorReceiptResponse(
        Long id,
        Long payoutId,
        String payoutReference,
        Long investorId,
        String investorCode,
        String investorName,
        BigDecimal payoutAmount,
        String receiptNumber,
        String documentUrl,
        Integer version,
        String generatedBy,
        LocalDateTime generatedAt
) {
}

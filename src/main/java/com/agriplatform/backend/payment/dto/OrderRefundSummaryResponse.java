package com.agriplatform.backend.payment.dto;

import java.math.BigDecimal;

public record OrderRefundSummaryResponse(
        String status,
        BigDecimal refundedAmount,
        BigDecimal pendingAmount,
        BigDecimal refundableAmount
) {
}

package com.agriplatform.backend.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CashfreePaymentLinkResult(
        String merchantLinkId,
        String providerLinkId,
        String linkUrl,
        String status,
        BigDecimal amount,
        BigDecimal amountPaid,
        LocalDateTime expiresAt
) {
}

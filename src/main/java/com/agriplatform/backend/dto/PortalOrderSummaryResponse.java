package com.agriplatform.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortalOrderSummaryResponse(
        Long id,
        String orderNumber,
        String status,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime createdAt,
        String quoteReference
) {
}

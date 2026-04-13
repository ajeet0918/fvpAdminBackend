package com.agriplatform.backend.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSlug,
        Integer quantity,
        String unit,
        String moqSnapshot,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal discountRate,
        BigDecimal discountAmount,
        BigDecimal lineTotal
) {
}

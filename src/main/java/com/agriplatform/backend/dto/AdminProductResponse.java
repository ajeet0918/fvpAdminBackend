package com.agriplatform.backend.dto;

import java.math.BigDecimal;

public record AdminProductResponse(
        Long id,
        String name,
        String slug,
        String sku,
        BigDecimal price,
        String priceUnit,
        BigDecimal defaultTaxRate,
        BigDecimal defaultDiscountRate,
        String status,
        String imageUrl,
        String shortDescription,
        String longDescription,
        String moq,
        boolean featured,
        Long categoryId,
        String categoryName
) {
}

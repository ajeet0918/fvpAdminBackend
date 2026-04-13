package com.agriplatform.backend.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String sku,
        BigDecimal price,
        String priceUnit,
        String status,
        String imageUrl,
        String shortDescription,
        String longDescription,
        String moq,
        boolean featured,
        String category
) {
}

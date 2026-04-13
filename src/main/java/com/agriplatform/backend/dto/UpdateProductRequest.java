package com.agriplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String slug,
        @NotBlank @Size(max = 120) String sku,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotBlank @Size(max = 30) String priceUnit,
        @NotNull @PositiveOrZero @DecimalMax("100.00") BigDecimal defaultTaxRate,
        @NotNull @PositiveOrZero @DecimalMax("100.00") BigDecimal defaultDiscountRate,
        @NotBlank @Size(max = 40) String status,
        @Size(max = 600) String imageUrl,
        @NotBlank @Size(max = 300) String shortDescription,
        @NotBlank @Size(max = 1200) String longDescription,
        @NotBlank @Size(max = 255) String moq,
        boolean featured,
        @NotNull Long categoryId
) {
}

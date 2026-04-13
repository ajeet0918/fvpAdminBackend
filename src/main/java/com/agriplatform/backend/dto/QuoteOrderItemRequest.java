package com.agriplatform.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuoteOrderItemRequest(
        @NotNull Long itemId,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @DecimalMin("0.00") BigDecimal taxRate,
        @DecimalMin("0.00") BigDecimal discountRate
) {
}

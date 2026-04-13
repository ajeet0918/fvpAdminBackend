package com.agriplatform.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record QuoteOrderRequest(
        @NotBlank String quoteReference,
        String adminNotes,
        @NotNull @DecimalMin("0.00") BigDecimal shippingAmount,
        @DecimalMin("0.00") BigDecimal taxAmount,
        @DecimalMin("0.00") BigDecimal discountAmount,
        @Valid @NotEmpty List<QuoteOrderItemRequest> items
) {
}

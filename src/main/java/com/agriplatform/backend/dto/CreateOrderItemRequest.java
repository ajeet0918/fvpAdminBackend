package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotBlank String productSlug,
        @NotNull @Min(1) Integer quantity,
        @NotBlank String unit
) {
}

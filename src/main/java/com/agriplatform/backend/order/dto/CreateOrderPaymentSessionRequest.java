package com.agriplatform.backend.order.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderPaymentSessionRequest(
        @NotBlank String checkoutSuccessUrl,
        @NotBlank String checkoutFailureUrl
) {
}

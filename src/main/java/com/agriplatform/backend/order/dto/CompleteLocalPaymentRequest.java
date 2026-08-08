package com.agriplatform.backend.order.dto;

import jakarta.validation.constraints.NotNull;

public record CompleteLocalPaymentRequest(
        @NotNull LocalPaymentOutcome outcome
) {
}

package com.agriplatform.backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteManualRefundRequest(
        @NotBlank @Size(max = 140) String reference,
        @Size(max = 600) String note
) {
}

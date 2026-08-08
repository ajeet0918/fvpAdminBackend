package com.agriplatform.backend.order.dto;

import com.agriplatform.backend.order.model.OrderPaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MarkOrderPaymentRequest(
        @NotNull OrderPaymentMethod paymentMethod,
        @NotBlank @Size(max = 140) String reference,
        @Size(max = 600) String note
) {
}

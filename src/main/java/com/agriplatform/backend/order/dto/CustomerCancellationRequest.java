package com.agriplatform.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerCancellationRequest(
        @NotBlank @Size(min = 5, max = 600) String reason
) {
}

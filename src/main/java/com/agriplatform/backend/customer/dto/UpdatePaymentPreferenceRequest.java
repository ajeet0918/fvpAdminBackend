package com.agriplatform.backend.customer.dto;

import jakarta.validation.constraints.Size;

public record UpdatePaymentPreferenceRequest(
        @Size(max = 40) String preferredPaymentMethod,
        @Size(max = 120) String preferredPaymentHandle
) {
}

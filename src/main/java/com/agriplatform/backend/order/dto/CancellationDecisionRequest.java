package com.agriplatform.backend.order.dto;

import jakarta.validation.constraints.Size;

public record CancellationDecisionRequest(
        boolean approved,
        @Size(max = 600) String note
) {
}

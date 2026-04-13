package com.agriplatform.backend.dto;

import com.agriplatform.backend.model.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull PurchaseOrderStatus status,
        String adminNotes
) {
}

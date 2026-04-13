package com.agriplatform.backend.dto;

import com.agriplatform.backend.model.PurchaseOrderStatus;
import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(
        PurchaseOrderStatus status,
        String note,
        LocalDateTime changedAt
) {
}

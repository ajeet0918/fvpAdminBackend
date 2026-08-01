package com.agriplatform.backend.payment.dto;

import com.agriplatform.backend.payment.model.OrderRefundStatus;
import java.time.LocalDateTime;

public record CashfreeRefundResult(
        String providerRefundId,
        String providerPaymentId,
        OrderRefundStatus status,
        String statusDescription,
        String refundArn,
        LocalDateTime processedAt
) {
}

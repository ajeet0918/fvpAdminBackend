package com.agriplatform.backend.payment.dto;

import com.agriplatform.backend.payment.model.OrderRefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundWebhookUpdate(
        String orderNumber,
        String refundId,
        String providerRefundId,
        String providerPaymentId,
        BigDecimal amount,
        String currency,
        OrderRefundStatus status,
        String speed,
        String note,
        String statusDescription,
        String refundArn,
        LocalDateTime processedAt
) {
}

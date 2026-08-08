package com.agriplatform.backend.payment.dto;

import com.agriplatform.backend.payment.model.OrderRefundStatus;
import com.agriplatform.backend.payment.model.OrderRefundMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderRefundResponse(
        Long id,
        String refundId,
        String providerRefundId,
        String providerPaymentId,
        BigDecimal amount,
        String currency,
        OrderRefundMethod refundMethod,
        OrderRefundStatus status,
        String speed,
        String note,
        String statusDescription,
        String refundArn,
        String requestedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime processedAt
) {
}

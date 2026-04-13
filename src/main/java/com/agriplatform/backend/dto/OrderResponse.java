package com.agriplatform.backend.dto;

import com.agriplatform.backend.model.PurchaseOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String orderNumber,
        String fullName,
        String companyName,
        String email,
        String phone,
        String deliveryAddress,
        String city,
        String state,
        String postalCode,
        String customerNotes,
        PurchaseOrderStatus status,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime quotedAt,
        LocalDateTime confirmedAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        String adminNotes,
        String quoteReference,
        BigDecimal subtotalAmount,
        BigDecimal shippingAmount,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal effectiveTaxRate,
        BigDecimal effectiveDiscountRate,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory
) {
}

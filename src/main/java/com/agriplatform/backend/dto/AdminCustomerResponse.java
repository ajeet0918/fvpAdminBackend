package com.agriplatform.backend.dto;

import java.time.LocalDateTime;

public record AdminCustomerResponse(
        Long id,
        String fullName,
        String companyName,
        String email,
        String phone,
        String deliveryAddress,
        String city,
        String state,
        String postalCode,
        boolean active,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long totalOrders,
        String lastOrderNumber,
        LocalDateTime lastOrderAt
) {
}

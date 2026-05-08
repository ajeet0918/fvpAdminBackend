package com.agriplatform.backend.customer.dto;

public record CustomerProfileResponse(
        Long id,
        String fullName,
        String companyName,
        String email,
        String phone,
        String deliveryAddress,
        String city,
        String state,
        String postalCode,
        String preferredPaymentMethod,
        String preferredPaymentHandle
) {
}

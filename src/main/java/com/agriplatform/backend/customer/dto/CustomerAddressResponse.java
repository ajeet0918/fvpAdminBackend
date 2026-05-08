package com.agriplatform.backend.customer.dto;

public record CustomerAddressResponse(
        Long id,
        String label,
        String recipientName,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,
        boolean isDefault
) {
}

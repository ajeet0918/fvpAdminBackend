package com.agriplatform.backend.dto;

import java.time.LocalDateTime;

public record LeadResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String companyName,
        String status,
        String source,
        String notes,
        String assignedTo,
        Long inquiryId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

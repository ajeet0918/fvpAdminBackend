package com.agriplatform.backend.dto;

import java.time.LocalDateTime;

public record PortalFarmerSummaryResponse(
        Long id,
        String referenceId,
        String status,
        String verificationStatus,
        String farmingType,
        String landArea,
        String mainCrops,
        String farmerActionNote,
        LocalDateTime createdAt
) {
}

package com.agriplatform.backend.dto;

public record ImageUploadResponse(
        String imageUrl,
        String fileName
) {
}

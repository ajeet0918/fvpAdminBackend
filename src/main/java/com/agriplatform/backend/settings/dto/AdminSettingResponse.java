package com.agriplatform.backend.settings.dto;

import com.agriplatform.backend.settings.model.AppSetting;
import com.agriplatform.backend.settings.model.AppSettingValueType;
import java.time.LocalDateTime;

public record AdminSettingResponse(
        Long id,
        String settingKey,
        String category,
        String value,
        AppSettingValueType valueType,
        boolean secret,
        boolean active,
        String description,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminSettingResponse from(AppSetting setting, String resolvedValue) {
        return new AdminSettingResponse(
                setting.getId(),
                setting.getSettingKey(),
                setting.getCategory(),
                resolvedValue,
                setting.getValueType(),
                setting.isSecret(),
                setting.isActive(),
                setting.getDescription(),
                setting.getUpdatedBy(),
                setting.getCreatedAt(),
                setting.getUpdatedAt()
        );
    }
}


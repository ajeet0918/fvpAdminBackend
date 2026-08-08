package com.agriplatform.backend.settings.dto;

import com.agriplatform.backend.settings.model.AppSettingValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertAdminSettingRequest(
        @NotBlank @Size(max = 160) String settingKey,
        @NotBlank @Size(max = 80) String category,
        @Size(max = 12000) String value,
        @NotNull AppSettingValueType valueType,
        boolean secret,
        boolean active,
        @Size(max = 500) String description
) {
}

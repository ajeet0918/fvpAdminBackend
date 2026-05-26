package com.agriplatform.backend.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SaveAdminSettingsRequest(
        @NotEmpty List<@Valid UpsertAdminSettingRequest> settings
) {
}


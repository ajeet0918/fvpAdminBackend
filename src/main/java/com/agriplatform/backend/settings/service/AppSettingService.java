package com.agriplatform.backend.settings.service;

import com.agriplatform.backend.settings.dto.AdminSettingResponse;
import com.agriplatform.backend.settings.dto.UpsertAdminSettingRequest;
import com.agriplatform.backend.settings.model.AppSetting;
import com.agriplatform.backend.settings.repository.AppSettingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppSettingService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppSettingService.class);

    private static final String SECRET_MASK = "********";

    private final AppSettingRepository appSettingRepository;

    public AppSettingService(AppSettingRepository appSettingRepository) {
        this.appSettingRepository = appSettingRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminSettingResponse> listSettings(String category) {
        List<AppSetting> settings = hasText(category)
                ? appSettingRepository.findByCategoryIgnoreCaseOrderBySettingKeyAsc(category.trim())
                : appSettingRepository.findAllByOrderByCategoryAscSettingKeyAsc();

        return settings.stream()
                .map(setting -> AdminSettingResponse.from(setting, resolveDisplayValue(setting)))
                .toList();
    }

    @Transactional
    public List<AdminSettingResponse> saveSettings(List<UpsertAdminSettingRequest> requests, String updatedBy) {
        List<AdminSettingResponse> responses = new ArrayList<>();
        for (UpsertAdminSettingRequest request : requests) {
            String normalizedKey = normalizeKey(request.settingKey());
            AppSetting setting = appSettingRepository.findBySettingKey(normalizedKey)
                    .orElseGet(() -> createSetting(request, normalizedKey, updatedBy));

            if (setting.getId() != null) {
                String nextValue = resolveNextValue(setting, request.value());
                setting.update(
                        normalizeText(request.category()),
                        nextValue,
                        request.valueType(),
                        request.secret(),
                        request.active(),
                        normalizeText(request.description()),
                        updatedBy
                );
            }

            AppSetting saved = appSettingRepository.save(setting);
            responses.add(AdminSettingResponse.from(saved, resolveDisplayValue(saved)));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public String getActiveValue(String settingKey) {
        if (!hasText(settingKey)) {
            return "";
        }
        Optional<AppSetting> setting = appSettingRepository.findBySettingKeyAndActiveTrue(normalizeKey(settingKey));
        if (setting.isEmpty()) {
            return "";
        }
        String value = setting.get().getSettingValue();
        return value == null ? "" : value.trim();
    }

    private AppSetting createSetting(UpsertAdminSettingRequest request, String normalizedKey, String updatedBy) {
        return new AppSetting(
                normalizedKey,
                normalizeText(request.category()),
                normalizeText(request.value()),
                request.valueType(),
                request.secret(),
                request.active(),
                normalizeText(request.description()),
                updatedBy
        );
    }

    private String resolveNextValue(AppSetting existing, String requestedValue) {
        String normalizedRequested = normalizeText(requestedValue);
        if (existing.isSecret() && (!hasText(normalizedRequested) || isSecretMask(normalizedRequested))) {
            return existing.getSettingValue();
        }
        return normalizedRequested;
    }

    private String resolveDisplayValue(AppSetting setting) {
        if (setting.isSecret()) {
            return hasText(setting.getSettingValue()) ? SECRET_MASK : "";
        }
        return setting.getSettingValue() == null ? "" : setting.getSettingValue();
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isSecretMask(String value) {
        return value.chars().allMatch(character -> character == '*');
    }
}


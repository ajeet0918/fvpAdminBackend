package com.agriplatform.backend.settings.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.settings.dto.UpsertAdminSettingRequest;
import com.agriplatform.backend.settings.model.AppSetting;
import com.agriplatform.backend.settings.model.AppSettingValueType;
import com.agriplatform.backend.settings.repository.AppSettingRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppSettingServiceTest {
    private static final String SETTING_KEY = "payment.cashfree.client-secret";
    private static final String STORED_SECRET = "actual-cashfree-secret";

    @Mock
    private AppSettingRepository appSettingRepository;

    @Mock
    private AppSetting setting;

    @Test
    void saveSettingsPreservesSecretWhenFourCharacterMaskIsSubmitted() {
        saveMaskedSecret("****");

        verifySecretWasPreserved();
    }

    @Test
    void saveSettingsPreservesSecretWhenEightCharacterMaskIsSubmitted() {
        saveMaskedSecret("********");

        verifySecretWasPreserved();
    }

    private void saveMaskedSecret(String mask) {
        when(appSettingRepository.findBySettingKey(SETTING_KEY)).thenReturn(Optional.of(setting));
        when(setting.getId()).thenReturn(4L);
        when(setting.isSecret()).thenReturn(true);
        when(setting.getSettingValue()).thenReturn(STORED_SECRET);
        when(appSettingRepository.save(setting)).thenReturn(setting);
        AppSettingService service = new AppSettingService(appSettingRepository);

        service.saveSettings(List.of(request(mask)), "stageadmin");
    }

    private void verifySecretWasPreserved() {
        verify(setting).update(
                "PAYMENT",
                STORED_SECRET,
                AppSettingValueType.STRING,
                true,
                true,
                "Cashfree client secret",
                "stageadmin"
        );
    }

    private UpsertAdminSettingRequest request(String value) {
        return new UpsertAdminSettingRequest(
                SETTING_KEY,
                "PAYMENT",
                value,
                AppSettingValueType.STRING,
                true,
                true,
                "Cashfree client secret"
        );
    }
}

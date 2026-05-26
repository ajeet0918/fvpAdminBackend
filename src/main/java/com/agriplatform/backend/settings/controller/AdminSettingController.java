package com.agriplatform.backend.settings.controller;

import com.agriplatform.backend.settings.dto.AdminSettingResponse;
import com.agriplatform.backend.settings.dto.SaveSmtpConfigRequest;
import com.agriplatform.backend.settings.dto.SaveAdminSettingsRequest;
import com.agriplatform.backend.settings.dto.SmtpConfigResponse;
import com.agriplatform.backend.settings.service.AppSettingService;
import com.agriplatform.backend.settings.service.SmtpConfigService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminSettingController.class);

    private final AppSettingService appSettingService;
    private final SmtpConfigService smtpConfigService;

    public AdminSettingController(AppSettingService appSettingService, SmtpConfigService smtpConfigService) {
        this.appSettingService = appSettingService;
        this.smtpConfigService = smtpConfigService;
    }

    @GetMapping
    public List<AdminSettingResponse> listSettings(@RequestParam(required = false) String category) {
        return appSettingService.listSettings(category);
    }

    @PostMapping("/bulk")
    public List<AdminSettingResponse> saveSettings(
            Authentication authentication,
            @Valid @RequestBody SaveAdminSettingsRequest request
    ) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        return appSettingService.saveSettings(request.settings(), updatedBy);
    }

    @GetMapping("/smtp")
    public SmtpConfigResponse getSmtpConfig() {
        return smtpConfigService.getConfig();
    }

    @PutMapping("/smtp")
    public SmtpConfigResponse saveSmtpConfig(
            Authentication authentication,
            @Valid @RequestBody SaveSmtpConfigRequest request
    ) {
        String updatedBy = authentication != null ? authentication.getName() : "system";
        return smtpConfigService.saveConfig(request, updatedBy);
    }
}

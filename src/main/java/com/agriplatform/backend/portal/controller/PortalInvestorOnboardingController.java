package com.agriplatform.backend.portal.controller;

import com.agriplatform.backend.investor.service.InvestorAgreementService;
import com.agriplatform.backend.portal.dto.PortalInvestorOnboardingResponse;
import com.agriplatform.backend.portal.service.PortalInvestorOnboardingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/investor")
public class PortalInvestorOnboardingController {
    private final PortalInvestorOnboardingService portalInvestorOnboardingService;

    public PortalInvestorOnboardingController(PortalInvestorOnboardingService portalInvestorOnboardingService) {
        this.portalInvestorOnboardingService = portalInvestorOnboardingService;
    }

    @GetMapping("/onboarding")
    public PortalInvestorOnboardingResponse getOnboarding(Authentication authentication) {
        return portalInvestorOnboardingService.get(authentication == null ? "" : authentication.getName());
    }

    @GetMapping("/agreements/{agreementId}/download")
    public ResponseEntity<byte[]> downloadAgreement(
            Authentication authentication,
            @PathVariable Long agreementId
    ) {
        InvestorAgreementService.AgreementDocument document = portalInvestorOnboardingService.download(
                authentication == null ? "" : authentication.getName(),
                agreementId
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.filename() + "\"")
                .body(document.content());
    }
}

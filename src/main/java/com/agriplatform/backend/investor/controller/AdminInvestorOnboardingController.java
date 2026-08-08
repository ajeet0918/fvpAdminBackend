package com.agriplatform.backend.investor.controller;

import com.agriplatform.backend.investor.dto.ApproveInvestorOnboardingRequest;
import com.agriplatform.backend.investor.dto.InvestorOnboardingResponse;
import com.agriplatform.backend.investor.model.InvestorAgreement;
import com.agriplatform.backend.investor.service.InvestorAgreementService;
import com.agriplatform.backend.investor.service.InvestorOnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inquiries/{inquiryId}/investor-onboarding")
public class AdminInvestorOnboardingController {
    private final InvestorOnboardingService investorOnboardingService;
    private final InvestorAgreementService investorAgreementService;

    public AdminInvestorOnboardingController(
            InvestorOnboardingService investorOnboardingService,
            InvestorAgreementService investorAgreementService
    ) {
        this.investorOnboardingService = investorOnboardingService;
        this.investorAgreementService = investorAgreementService;
    }

    @GetMapping
    public InvestorOnboardingResponse get(@PathVariable Long inquiryId) {
        return investorOnboardingService.get(inquiryId);
    }

    @PostMapping("/approve")
    public InvestorOnboardingResponse approve(
            @PathVariable Long inquiryId,
            @Valid @RequestBody ApproveInvestorOnboardingRequest request
    ) {
        return investorOnboardingService.approve(inquiryId, request);
    }

    @PostMapping("/resend-payment-email")
    public InvestorOnboardingResponse resendPaymentEmail(@PathVariable Long inquiryId) {
        return investorOnboardingService.resendPaymentEmail(inquiryId);
    }

    @PostMapping("/resend-portal-invite")
    public void resendPortalInvite(@PathVariable Long inquiryId) {
        investorOnboardingService.resendPortalInvite(inquiryId);
    }

    @GetMapping("/agreement")
    public ResponseEntity<byte[]> downloadAgreement(@PathVariable Long inquiryId) {
        InvestorAgreement agreement = investorAgreementService.getByInquiryId(inquiryId);
        InvestorAgreementService.AgreementDocument document = investorAgreementService.buildPdf(agreement);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.filename() + "\"")
                .body(document.content());
    }
}

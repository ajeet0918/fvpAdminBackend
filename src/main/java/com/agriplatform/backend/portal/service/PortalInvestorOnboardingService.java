package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.investor.model.InvestorAgreement;
import com.agriplatform.backend.investor.model.InvestorAgreementStatus;
import com.agriplatform.backend.investor.model.InvestorPayment;
import com.agriplatform.backend.investor.repository.InvestorAgreementRepository;
import com.agriplatform.backend.investor.repository.InvestorPaymentRepository;
import com.agriplatform.backend.investor.service.InvestorAgreementService;
import com.agriplatform.backend.portal.dto.PortalInvestorOnboardingResponse;
import com.agriplatform.backend.portal.model.PortalUser;
import com.agriplatform.backend.portal.model.PortalUserType;
import com.agriplatform.backend.portal.repository.PortalUserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalInvestorOnboardingService {
    private static final String PORTAL_SUBJECT_PREFIX = "PORTAL:";

    private final PortalUserRepository portalUserRepository;
    private final InvestorPaymentRepository investorPaymentRepository;
    private final InvestorAgreementRepository investorAgreementRepository;
    private final InvestorAgreementService investorAgreementService;

    public PortalInvestorOnboardingService(
            PortalUserRepository portalUserRepository,
            InvestorPaymentRepository investorPaymentRepository,
            InvestorAgreementRepository investorAgreementRepository,
            InvestorAgreementService investorAgreementService
    ) {
        this.portalUserRepository = portalUserRepository;
        this.investorPaymentRepository = investorPaymentRepository;
        this.investorAgreementRepository = investorAgreementRepository;
        this.investorAgreementService = investorAgreementService;
    }

    @Transactional(readOnly = true)
    public PortalInvestorOnboardingResponse get(String subject) {
        PortalUser user = getInvestorUser(subject);
        InvestorPayment payment = investorPaymentRepository.findBySourceInquiryId(user.getSourceInquiryId())
                .orElseThrow(() -> new IllegalArgumentException("Investor onboarding details not found"));
        return new PortalInvestorOnboardingResponse(
                payment.getInvestorAccount().getInvestorCode(),
                payment.getInvestorAccount().getStatus().name(),
                payment.getInvestment().getInvestmentReference(),
                payment.getInvestment().getStatus().name(),
                payment.getAmount(),
                payment.getInvestment().getMonthlyReturnRate(),
                payment.getStatus().name(),
                payment.getAmountPaid(),
                payment.getPaidAt(),
                investorAgreementRepository.findByInvestorAccount_IdOrderByCreatedAtDesc(
                        payment.getInvestorAccount().getId()
                        ).stream()
                        .filter(agreement -> agreement.getInvestorPayment().getSourceInquiryId()
                                .equals(user.getSourceInquiryId()))
                        .map(this::mapAgreement)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public InvestorAgreementService.AgreementDocument download(String subject, Long agreementId) {
        PortalUser user = getInvestorUser(subject);
        InvestorAgreement agreement = investorAgreementService.getById(agreementId);
        if (!agreement.getInvestorPayment().getSourceInquiryId().equals(user.getSourceInquiryId())) {
            throw new AccessDeniedException("Agreement does not belong to this portal account");
        }
        return investorAgreementService.buildPdf(agreement);
    }

    private PortalInvestorOnboardingResponse.AgreementSummary mapAgreement(InvestorAgreement agreement) {
        String downloadUrl = agreement.getStatus() == InvestorAgreementStatus.AVAILABLE
                ? "/api/portal/investor/agreements/" + agreement.getId() + "/download"
                : null;
        return new PortalInvestorOnboardingResponse.AgreementSummary(
                agreement.getId(),
                agreement.getAgreementNumber(),
                agreement.getStatus().name(),
                agreement.getGeneratedAt(),
                downloadUrl
        );
    }

    private PortalUser getInvestorUser(String subject) {
        if (subject == null || !subject.startsWith(PORTAL_SUBJECT_PREFIX)) {
            throw new AccessDeniedException("Investor portal authentication is required");
        }
        Long userId;
        try {
            userId = Long.parseLong(subject.substring(PORTAL_SUBJECT_PREFIX.length()));
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid investor portal authentication");
        }
        PortalUser user = portalUserRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("Portal account not found"));
        if (user.getUserType() != PortalUserType.INVESTOR || user.getSourceInquiryId() == null) {
            throw new AccessDeniedException("This portal account is not an investor account");
        }
        return user;
    }
}

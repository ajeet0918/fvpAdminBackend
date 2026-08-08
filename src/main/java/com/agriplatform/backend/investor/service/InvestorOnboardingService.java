package com.agriplatform.backend.investor.service;

import com.agriplatform.backend.inquiry.model.Inquiry;
import com.agriplatform.backend.inquiry.model.InquiryStatus;
import com.agriplatform.backend.inquiry.model.InquiryType;
import com.agriplatform.backend.inquiry.model.PaymentStatus;
import com.agriplatform.backend.inquiry.model.VerificationStatus;
import com.agriplatform.backend.inquiry.repository.InquiryRepository;
import com.agriplatform.backend.investor.dto.ApproveInvestorOnboardingRequest;
import com.agriplatform.backend.investor.dto.InvestorOnboardingResponse;
import com.agriplatform.backend.investor.model.Investment;
import com.agriplatform.backend.investor.model.InvestmentStatus;
import com.agriplatform.backend.investor.model.InvestorAccount;
import com.agriplatform.backend.investor.model.InvestorAccountStatus;
import com.agriplatform.backend.investor.model.InvestorAgreement;
import com.agriplatform.backend.investor.model.InvestorAgreementStatus;
import com.agriplatform.backend.investor.model.InvestorPayment;
import com.agriplatform.backend.investor.model.InvestorPaymentEvent;
import com.agriplatform.backend.investor.model.InvestorPaymentStatus;
import com.agriplatform.backend.investor.repository.InvestmentRepository;
import com.agriplatform.backend.investor.repository.InvestorAccountRepository;
import com.agriplatform.backend.investor.repository.InvestorAgreementRepository;
import com.agriplatform.backend.investor.repository.InvestorPaymentEventRepository;
import com.agriplatform.backend.investor.repository.InvestorPaymentRepository;
import com.agriplatform.backend.payment.dto.CashfreePaymentLinkResult;
import com.agriplatform.backend.payment.dto.CashfreePaymentLinkWebhookPayload;
import com.agriplatform.backend.payment.service.CashfreeApiConstants;
import com.agriplatform.backend.payment.service.CashfreePaymentLinkService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestorOnboardingService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final InquiryRepository inquiryRepository;
    private final InvestorAccountRepository investorAccountRepository;
    private final InvestmentRepository investmentRepository;
    private final InvestorPaymentRepository investorPaymentRepository;
    private final InvestorPaymentEventRepository investorPaymentEventRepository;
    private final InvestorAgreementRepository investorAgreementRepository;
    private final InvestorOnboardingSettings onboardingSettings;
    private final CashfreePaymentLinkService cashfreePaymentLinkService;
    private final InvestorPaymentMailService investorPaymentMailService;
    private final InvestorAgreementService investorAgreementService;
    private final InvestorPostPaymentService investorPostPaymentService;
    private final ApplicationEventPublisher eventPublisher;

    public InvestorOnboardingService(
            InquiryRepository inquiryRepository,
            InvestorAccountRepository investorAccountRepository,
            InvestmentRepository investmentRepository,
            InvestorPaymentRepository investorPaymentRepository,
            InvestorPaymentEventRepository investorPaymentEventRepository,
            InvestorAgreementRepository investorAgreementRepository,
            InvestorOnboardingSettings onboardingSettings,
            CashfreePaymentLinkService cashfreePaymentLinkService,
            InvestorPaymentMailService investorPaymentMailService,
            InvestorAgreementService investorAgreementService,
            InvestorPostPaymentService investorPostPaymentService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.inquiryRepository = inquiryRepository;
        this.investorAccountRepository = investorAccountRepository;
        this.investmentRepository = investmentRepository;
        this.investorPaymentRepository = investorPaymentRepository;
        this.investorPaymentEventRepository = investorPaymentEventRepository;
        this.investorAgreementRepository = investorAgreementRepository;
        this.onboardingSettings = onboardingSettings;
        this.cashfreePaymentLinkService = cashfreePaymentLinkService;
        this.investorPaymentMailService = investorPaymentMailService;
        this.investorAgreementService = investorAgreementService;
        this.investorPostPaymentService = investorPostPaymentService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public InvestorOnboardingResponse approve(Long inquiryId, ApproveInvestorOnboardingRequest request) {
        Inquiry inquiry = getInvestorInquiryLocked(inquiryId);
        validateApproval(inquiry, request);

        return investorPaymentRepository.findBySourceInquiryId(inquiryId)
                .map(this::map)
                .orElseGet(() -> createOnboarding(inquiry, request));
    }

    @Transactional(readOnly = true)
    public InvestorOnboardingResponse get(Long inquiryId) {
        getInvestorInquiry(inquiryId);
        InvestorPayment payment = investorPaymentRepository.findBySourceInquiryId(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Investor onboarding has not been approved yet"));
        return map(payment);
    }

    @Transactional
    public InvestorOnboardingResponse resendPaymentEmail(Long inquiryId) {
        Inquiry inquiry = getInvestorInquiry(inquiryId);
        InvestorPayment payment = investorPaymentRepository.findBySourceInquiryId(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Investor onboarding has not been approved yet"));
        if (payment.getStatus() == InvestorPaymentStatus.PAID) {
            throw new IllegalArgumentException("Investment payment is already complete");
        }
        if (payment.getStatus() == InvestorPaymentStatus.CANCELLED
                || payment.getStatus() == InvestorPaymentStatus.EXPIRED
                || payment.getLinkExpiresAt() == null
                || payment.getLinkExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Payment link is no longer active; approve a replacement workflow before resending");
        }
        InvestorOnboardingSettings.Snapshot settings = onboardingSettings.load();
        sendPaymentEmail(inquiry, payment, settings);
        return map(payment);
    }

    public void resendPortalInvite(Long inquiryId) {
        getInvestorInquiry(inquiryId);
        InvestorPayment payment = investorPaymentRepository.findBySourceInquiryId(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Investor onboarding has not been approved yet"));
        if (payment.getStatus() != InvestorPaymentStatus.PAID) {
            throw new IllegalArgumentException("Portal invite can only be sent after full payment verification");
        }
        investorPostPaymentService.sendPortalInviteNow(
                new InvestorPaymentCompletedEvent(payment.getId(), payment.getSourceInquiryId())
        );
    }

    @Transactional
    public void processWebhook(
            CashfreePaymentLinkWebhookPayload payload,
            String eventKey,
            String payloadSnapshot
    ) {
        validateWebhookPayload(payload);
        CashfreePaymentLinkWebhookPayload.Data data = payload.data();
        InvestorPayment payment = investorPaymentRepository.findLockedByMerchantLinkId(data.merchantLinkId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown Cashfree investor payment link"));

        if (investorPaymentEventRepository.existsByEventKey(eventKey)) {
            return;
        }
        validatePaymentIdentity(payment, data);

        LocalDateTime eventTime = parseEventTime(payload.eventTime());
        String paymentReference = data.order() == null ? null : firstText(
                data.order().transactionId(), data.order().orderId()
        );
        investorPaymentEventRepository.save(new InvestorPaymentEvent(
                payment,
                eventKey,
                payload.type(),
                data.linkStatus(),
                data.amountPaid(),
                paymentReference,
                eventTime,
                truncate(payloadSnapshot, 4000)
        ));

        if (payment.getStatus() == InvestorPaymentStatus.PAID) {
            return;
        }

        InvestorPaymentStatus nextStatus = parseLinkStatus(data.linkStatus());
        boolean newlyPaid = payment.getStatus() != InvestorPaymentStatus.PAID
                && nextStatus == InvestorPaymentStatus.PAID;
        if (nextStatus == InvestorPaymentStatus.PAID && data.amountPaid().compareTo(payment.getAmount()) < 0) {
            throw new IllegalArgumentException("Cashfree paid amount is less than the approved investment amount");
        }
        payment.updatePayment(
                nextStatus,
                data.amountPaid(),
                paymentReference,
                nextStatus == InvestorPaymentStatus.PAID ? eventTime : null
        );
        investorPaymentRepository.save(payment);

        if (newlyPaid) {
            activateInvestment(payment);
            eventPublisher.publishEvent(new InvestorPaymentCompletedEvent(payment.getId(), payment.getSourceInquiryId()));
        }
    }

    private InvestorOnboardingResponse createOnboarding(
            Inquiry inquiry,
            ApproveInvestorOnboardingRequest request
    ) {
        InvestorOnboardingSettings.Snapshot settings = onboardingSettings.load();
        InvestorAccount investor = investorAccountRepository.findBySourceInquiryId(inquiry.getId())
                .orElseGet(() -> investorAccountRepository.save(new InvestorAccount(
                        generateInvestorCode(),
                        inquiry.getFullName().trim(),
                        inquiry.getEmail().trim().toLowerCase(Locale.ROOT),
                        inquiry.getPhone().trim(),
                        inquiry.getId(),
                        InvestorAccountStatus.PENDING_PAYMENT,
                        VerificationStatus.VERIFIED,
                        normalize(request.notes())
                )));

        Investment investment = investmentRepository.save(new Investment(
                investor,
                generateInvestmentReference(),
                money(inquiry.getInvestmentAmount()),
                request.monthlyReturnRate().setScale(2, RoundingMode.HALF_UP),
                request.investmentStartDate(),
                request.investmentEndDate(),
                InvestmentStatus.PENDING_PAYMENT,
                normalize(request.notes())
        ));

        String merchantLinkId = buildMerchantLinkId(inquiry);
        CashfreePaymentLinkResult link = cashfreePaymentLinkService.createInvestorPaymentLink(
                merchantLinkId,
                inquiry,
                investment.getPrincipalAmount(),
                settings
        );
        if (link.amount() != null && link.amount().compareTo(investment.getPrincipalAmount()) != 0) {
            throw new IllegalArgumentException("Cashfree returned an unexpected investor payment-link amount");
        }

        InvestorPayment payment = investorPaymentRepository.save(new InvestorPayment(
                investor,
                investment,
                inquiry.getId(),
                merchantLinkId,
                link.providerLinkId(),
                link.linkUrl(),
                investment.getPrincipalAmount(),
                CashfreeApiConstants.CURRENCY_INR,
                link.expiresAt()
        ));
        InvestorAgreement agreement = investorAgreementService.createPending(
                inquiry, investor, investment, payment, settings
        );
        inquiry.updateStatus(
                InquiryStatus.IN_PROGRESS,
                VerificationStatus.VERIFIED,
                PaymentStatus.PENDING,
                inquiry.getAdminNotes(),
                inquiry.getAssignedTo(),
                agreement.getAgreementNumber(),
                null,
                inquiry.getFarmerActionNote(),
                inquiry.getHubActionNote()
        );
        inquiryRepository.save(inquiry);
        sendPaymentEmail(inquiry, payment, settings);
        return map(payment);
    }

    private void activateInvestment(InvestorPayment payment) {
        InvestorAccount investor = payment.getInvestorAccount();
        investor.updateProfile(
                investor.getFullName(),
                investor.getEmail(),
                investor.getPhone(),
                InvestorAccountStatus.ACTIVE,
                VerificationStatus.VERIFIED,
                investor.getNotes()
        );
        investorAccountRepository.save(investor);

        Investment investment = payment.getInvestment();
        investment.update(
                investment.getPrincipalAmount(),
                investment.getMonthlyReturnRate(),
                investment.getStartDate(),
                investment.getEndDate(),
                InvestmentStatus.ACTIVE,
                investment.getNotes()
        );
        investmentRepository.save(investment);
        investorAgreementService.finalizeAgreement(payment);

        InvestorAgreement agreement = investorAgreementRepository.findByInvestorPayment_Id(payment.getId())
                .orElseThrow(() -> new IllegalArgumentException("Investor agreement not found"));
        Inquiry inquiry = getInvestorInquiry(payment.getSourceInquiryId());
        BigDecimal committedMonthlyReturn = investment.getPrincipalAmount()
                .multiply(investment.getMonthlyReturnRate())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        inquiry.updateStatus(
                InquiryStatus.CLOSED,
                VerificationStatus.VERIFIED,
                PaymentStatus.VERIFIED,
                inquiry.getAdminNotes(),
                inquiry.getAssignedTo(),
                agreement.getAgreementNumber(),
                committedMonthlyReturn,
                inquiry.getFarmerActionNote(),
                inquiry.getHubActionNote()
        );
        inquiryRepository.save(inquiry);
    }

    private void sendPaymentEmail(
            Inquiry inquiry,
            InvestorPayment payment,
            InvestorOnboardingSettings.Snapshot settings
    ) {
        try {
            investorPaymentMailService.sendPaymentLink(inquiry, payment, settings);
            payment.markEmailSent();
        } catch (RuntimeException ex) {
            payment.markEmailFailed(ex.getMessage());
        }
        investorPaymentRepository.save(payment);
    }

    private InvestorOnboardingResponse map(InvestorPayment payment) {
        InvestorAccount investor = payment.getInvestorAccount();
        Investment investment = payment.getInvestment();
        InvestorAgreement agreement = investorAgreementRepository.findByInvestorPayment_Id(payment.getId()).orElse(null);
        String downloadUrl = agreement != null && agreement.getStatus() == InvestorAgreementStatus.AVAILABLE
                ? "/api/admin/inquiries/" + payment.getSourceInquiryId() + "/investor-onboarding/agreement"
                : null;
        return new InvestorOnboardingResponse(
                payment.getSourceInquiryId(),
                investor.getId(),
                investor.getInvestorCode(),
                investor.getStatus().name(),
                investor.getVerificationStatus().name(),
                investment.getId(),
                investment.getInvestmentReference(),
                investment.getStatus().name(),
                investment.getPrincipalAmount(),
                investment.getMonthlyReturnRate(),
                investment.getStartDate(),
                investment.getEndDate(),
                payment.getId(),
                payment.getMerchantLinkId(),
                payment.getLinkUrl(),
                payment.getStatus().name(),
                payment.getAmountPaid(),
                payment.getLinkExpiresAt(),
                payment.getEmailDeliveryStatus().name(),
                payment.getEmailSentAt(),
                payment.getEmailError(),
                payment.getPortalInviteStatus().name(),
                payment.getPortalInviteSentAt(),
                payment.getPortalInviteError(),
                agreement == null ? null : agreement.getId(),
                agreement == null ? null : agreement.getAgreementNumber(),
                agreement == null ? null : agreement.getStatus().name(),
                downloadUrl,
                agreement == null ? null : agreement.getGeneratedAt(),
                agreement == null ? null : agreement.getGenerationError()
        );
    }

    private Inquiry getInvestorInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));
        if (inquiry.getInquiryType() != InquiryType.INVESTOR) {
            throw new IllegalArgumentException("This workflow is only available for investor inquiries");
        }
        return inquiry;
    }

    private Inquiry getInvestorInquiryLocked(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findLockedById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));
        if (inquiry.getInquiryType() != InquiryType.INVESTOR) {
            throw new IllegalArgumentException("This workflow is only available for investor inquiries");
        }
        return inquiry;
    }

    private void validateApproval(Inquiry inquiry, ApproveInvestorOnboardingRequest request) {
        if (inquiry.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("Verify the investor documents before approval");
        }
        if (!inquiry.isTermsAccepted()) {
            throw new IllegalArgumentException("Investor terms must be accepted before approval");
        }
        if (inquiry.getInvestmentAmount() == null || inquiry.getInvestmentAmount().signum() <= 0) {
            throw new IllegalArgumentException("A valid investment amount is required before approval");
        }
        if (request.investmentEndDate() != null
                && request.investmentEndDate().isBefore(request.investmentStartDate())) {
            throw new IllegalArgumentException("Investment end date cannot be before start date");
        }
        requireText(inquiry.getFullName(), "Investor name");
        requireText(inquiry.getEmail(), "Investor email");
        requireText(inquiry.getPhone(), "Investor phone");
        requireText(inquiry.getFullAddress(), "Investor address");
        requireText(inquiry.getPanNumber(), "Investor PAN");
    }

    private void validateWebhookPayload(CashfreePaymentLinkWebhookPayload payload) {
        if (payload == null || payload.data() == null) {
            throw new IllegalArgumentException("Cashfree payment-link webhook data is missing");
        }
        if (!"PAYMENT_LINK_EVENT".equalsIgnoreCase(payload.type())) {
            throw new IllegalArgumentException("Unsupported Cashfree payment-link event type");
        }
        requireText(payload.data().merchantLinkId(), "Cashfree merchant link ID");
        requireText(payload.data().linkStatus(), "Cashfree link status");
        if (payload.data().amount() == null || payload.data().amountPaid() == null) {
            throw new IllegalArgumentException("Cashfree payment-link amounts are missing");
        }
    }

    private void validatePaymentIdentity(InvestorPayment payment, CashfreePaymentLinkWebhookPayload.Data data) {
        if (!payment.getCurrency().equalsIgnoreCase(data.currency())) {
            throw new IllegalArgumentException("Cashfree payment-link currency does not match");
        }
        if (payment.getAmount().compareTo(data.amount()) != 0) {
            throw new IllegalArgumentException("Cashfree payment-link amount does not match");
        }
        if (payment.getProviderLinkId() != null && data.providerLinkId() != null
                && !payment.getProviderLinkId().equals(data.providerLinkId())) {
            throw new IllegalArgumentException("Cashfree provider link ID does not match");
        }
    }

    private InvestorPaymentStatus parseLinkStatus(String status) {
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PAID" -> InvestorPaymentStatus.PAID;
            case "PARTIALLY_PAID" -> InvestorPaymentStatus.PARTIALLY_PAID;
            case "EXPIRED" -> InvestorPaymentStatus.EXPIRED;
            case "CANCELLED" -> InvestorPaymentStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Unsupported Cashfree payment-link status");
        };
    }

    private LocalDateTime parseEventTime(String eventTime) {
        if (eventTime == null || eventTime.isBlank()) {
            throw new IllegalArgumentException("Invalid Cashfree payment-link event time");
        }
        try {
            return OffsetDateTime.parse(eventTime).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid Cashfree payment-link event time", ex);
        }
    }

    private String generateInvestorCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = "INV-" + shortUuid();
            if (!investorAccountRepository.existsByInvestorCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate a unique investor code");
    }

    private String generateInvestmentReference() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String reference = "IVT-" + shortUuid();
            if (!investmentRepository.existsByInvestmentReference(reference)) {
                return reference;
            }
        }
        throw new IllegalStateException("Unable to generate a unique investment reference");
    }

    private String buildMerchantLinkId(Inquiry inquiry) {
        String reference = inquiry.getReferenceId().replaceAll("[^A-Za-z0-9_-]", "");
        String value = "INVESTOR-" + inquiry.getId() + "-" + reference;
        return value.length() <= 80 ? value : value.substring(0, 80);
    }

    private String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
    }
}

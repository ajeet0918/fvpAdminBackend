package com.agriplatform.backend.inquiry.service;

import com.agriplatform.backend.*;
import com.agriplatform.backend.auth.controller.*;
import com.agriplatform.backend.auth.dto.*;
import com.agriplatform.backend.auth.service.*;
import com.agriplatform.backend.category.controller.*;
import com.agriplatform.backend.category.model.*;
import com.agriplatform.backend.category.repository.*;
import com.agriplatform.backend.common.controller.*;
import com.agriplatform.backend.config.*;
import com.agriplatform.backend.customer.controller.*;
import com.agriplatform.backend.customer.dto.*;
import com.agriplatform.backend.customer.model.*;
import com.agriplatform.backend.customer.repository.*;
import com.agriplatform.backend.customer.service.*;
import com.agriplatform.backend.document.controller.*;
import com.agriplatform.backend.document.dto.*;
import com.agriplatform.backend.document.model.*;
import com.agriplatform.backend.document.repository.*;
import com.agriplatform.backend.document.service.*;
import com.agriplatform.backend.inquiry.controller.*;
import com.agriplatform.backend.inquiry.dto.*;
import com.agriplatform.backend.inquiry.model.*;
import com.agriplatform.backend.inquiry.repository.*;
import com.agriplatform.backend.inquiry.service.*;
import com.agriplatform.backend.investor.controller.*;
import com.agriplatform.backend.investor.dto.*;
import com.agriplatform.backend.investor.model.*;
import com.agriplatform.backend.investor.repository.*;
import com.agriplatform.backend.investor.service.*;
import com.agriplatform.backend.lead.controller.*;
import com.agriplatform.backend.lead.dto.*;
import com.agriplatform.backend.lead.model.*;
import com.agriplatform.backend.lead.repository.*;
import com.agriplatform.backend.lead.service.*;
import com.agriplatform.backend.order.controller.*;
import com.agriplatform.backend.order.dto.*;
import com.agriplatform.backend.order.model.*;
import com.agriplatform.backend.order.repository.*;
import com.agriplatform.backend.order.service.*;
import com.agriplatform.backend.portal.controller.*;
import com.agriplatform.backend.portal.dto.*;
import com.agriplatform.backend.portal.model.*;
import com.agriplatform.backend.portal.repository.*;
import com.agriplatform.backend.portal.service.*;
import com.agriplatform.backend.product.controller.*;
import com.agriplatform.backend.product.dto.*;
import com.agriplatform.backend.product.model.*;
import com.agriplatform.backend.product.repository.*;
import com.agriplatform.backend.product.service.*;
import com.agriplatform.backend.security.*;
import com.agriplatform.backend.user.controller.*;
import com.agriplatform.backend.user.dto.*;
import com.agriplatform.backend.user.model.*;
import com.agriplatform.backend.user.repository.*;
import com.agriplatform.backend.user.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InquiryService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InquiryService.class);

    private final InquiryRepository inquiryRepository;
    private final LeadService leadService;
    private final InquiryDocumentStorageService inquiryDocumentStorageService;

    public InquiryService(
            InquiryRepository inquiryRepository,
            LeadService leadService,
            InquiryDocumentStorageService inquiryDocumentStorageService
    ) {
        this.inquiryRepository = inquiryRepository;
        this.leadService = leadService;
        this.inquiryDocumentStorageService = inquiryDocumentStorageService;
    }

    @Transactional
    public InquiryResponse createInquiry(InquiryRequest request) {
        Inquiry inquiry = new Inquiry(
                generateReferenceId(InquiryType.GENERAL),
                request.fullName().trim(),
                request.companyName().trim(),
                request.email().trim().toLowerCase(),
                request.phone().trim(),
                request.productName().trim(),
                request.message().trim()
        );
        return mapInquiry(inquiryRepository.save(inquiry));
    }

    @Transactional
    public InquiryResponse createInvestorInquiry(
            InvestorInquiryRequest request,
            MultipartFile idProof,
            MultipartFile paymentScreenshot
    ) {
        LocalDate investmentDate = parseDate(request.investmentDate(), "investmentDate");
        LocalDate paymentDate = parseDateNullable(request.paymentDate(), "paymentDate");
        InquiryDocumentStorageService.StoredDocument idProofDocument = inquiryDocumentStorageService.store(idProof, "investor");
        InquiryDocumentStorageService.StoredDocument paymentScreenshotDocument =
                inquiryDocumentStorageService.store(paymentScreenshot, "investor");

        String idProofUrl = idProofDocument != null ? idProofDocument.path() : null;
        String paymentScreenshotUrl = paymentScreenshotDocument != null ? paymentScreenshotDocument.path() : null;

        String transactionId = normalizeNullable(request.transactionId());
        boolean hasPaymentEvidence = transactionId != null || paymentDate != null || paymentScreenshotUrl != null;
        String agreementId = hasPaymentEvidence ? generateAgreementId() : null;

        String summaryMessage = normalizeNullable(request.notes());
        if (summaryMessage == null) {
            summaryMessage = "Investor submission from public website.";
        }

        Inquiry inquiry = Inquiry.createInvestor(
                generateReferenceId(InquiryType.INVESTOR),
                request.fullName().trim(),
                request.fatherName().trim(),
                request.email().trim().toLowerCase(),
                request.mobileNumber().trim(),
                request.aadhaarNumber().trim(),
                request.panNumber().trim(),
                request.fullAddress().trim(),
                request.investmentAmount(),
                investmentDate,
                normalizeNullable(request.preferredPaymentMode()),
                transactionId,
                paymentDate,
                "WEBSITE_INVESTOR",
                summaryMessage,
                idProofUrl,
                buildDocumentMetadataJson(idProofDocument),
                paymentScreenshotUrl,
                buildDocumentMetadataJson(paymentScreenshotDocument),
                request.termsAccepted(),
                agreementId
        );
        inquiry.attachInvestorDocuments(
                resolveDocumentId(idProofDocument),
                resolveDocumentId(paymentScreenshotDocument)
        );
        return mapInquiry(inquiryRepository.save(inquiry));
    }

    @Transactional
    public InquiryResponse createFarmerInquiry(
            FarmerInquiryRequest request,
            MultipartFile aadhaarDocument,
            MultipartFile landProofDocument,
            MultipartFile bankPassbookDocument
    ) {
        if (aadhaarDocument == null || aadhaarDocument.isEmpty()) {
            throw new IllegalArgumentException("Aadhaar upload is required for farmer registration");
        }

        InquiryDocumentStorageService.StoredDocument aadhaar = inquiryDocumentStorageService.store(aadhaarDocument, "farmer");
        InquiryDocumentStorageService.StoredDocument landProof = inquiryDocumentStorageService.store(landProofDocument, "farmer");
        InquiryDocumentStorageService.StoredDocument bankPassbook =
                inquiryDocumentStorageService.store(bankPassbookDocument, "farmer");

        String aadhaarDocumentUrl = aadhaar != null ? aadhaar.path() : null;
        String landProofDocumentUrl = landProof != null ? landProof.path() : null;
        String bankPassbookDocumentUrl = bankPassbook != null ? bankPassbook.path() : null;

        String summaryMessage = normalizeNullable(request.notes());
        if (summaryMessage == null) {
            summaryMessage = "Farmer onboarding submission from public website.";
        }

        Inquiry inquiry = Inquiry.createFarmer(
                generateReferenceId(InquiryType.FARMER),
                request.fullName().trim(),
                request.fatherName().trim(),
                request.email().trim().toLowerCase(),
                request.mobileNumber().trim(),
                normalizeNullable(request.alternateNumber()),
                request.aadhaarNumber().trim(),
                request.address().trim(),
                request.village().trim(),
                request.district().trim(),
                request.state().trim(),
                request.pinCode().trim(),
                request.farmingType().trim(),
                request.landArea().trim(),
                request.mainCrops().trim(),
                request.irrigationType().trim(),
                normalizeNullable(request.bankAccountNumber()),
                normalizeUpperNullable(request.ifscCode()),
                "WEBSITE_FARMER",
                summaryMessage,
                aadhaarDocumentUrl,
                buildDocumentMetadataJson(aadhaar),
                landProofDocumentUrl,
                buildDocumentMetadataJson(landProof),
                bankPassbookDocumentUrl,
                buildDocumentMetadataJson(bankPassbook),
                request.termsAccepted()
        );
        inquiry.attachFarmerDocuments(
                resolveDocumentId(aadhaar),
                resolveDocumentId(landProof),
                resolveDocumentId(bankPassbook)
        );

        return mapInquiry(inquiryRepository.save(inquiry));
    }

    @Transactional
    public InquiryResponse createCollectionHubInquiry(
            CollectionHubInquiryRequest request,
            MultipartFile hubDocument
    ) {
        InquiryDocumentStorageService.StoredDocument hubDoc =
                inquiryDocumentStorageService.store(hubDocument, "collection-hub");
        String hubDocumentUrl = hubDoc != null ? hubDoc.path() : null;
        String summaryMessage = normalizeNullable(request.notes());
        if (summaryMessage == null) {
            summaryMessage = "Collection hub onboarding submission from public website.";
        }

        Inquiry inquiry = Inquiry.createCollectionHub(
                generateReferenceId(InquiryType.COLLECTION_HUB),
                request.fullName().trim(),
                request.fatherName().trim(),
                request.email().trim().toLowerCase(Locale.ROOT),
                request.mobileNumber().trim(),
                normalizeNullable(request.alternateNumber()),
                request.aadhaarNumber().trim(),
                request.address().trim(),
                request.village().trim(),
                request.district().trim(),
                request.state().trim(),
                request.pinCode().trim(),
                request.collectionHubName().trim(),
                request.storageType().trim(),
                request.capacityMt(),
                request.pickupRadiusKm(),
                request.operatingDays().trim(),
                "WEBSITE_COLLECTION_HUB",
                summaryMessage,
                hubDocumentUrl,
                buildDocumentMetadataJson(hubDoc),
                request.termsAccepted(),
                generateHubCode()
        );
        inquiry.attachCollectionHubDocument(resolveDocumentId(hubDoc));

        return mapInquiry(inquiryRepository.save(inquiry));
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> getInquiries(
            String search,
            String status,
            String source,
            String assignedTo,
            String inquiryType
    ) {
        String normalizedSearch = normalizeSearch(search);
        InquiryStatus statusFilter = parseStatusNullable(status);
        String sourceFilter = normalizeUpperNullable(source);
        String assignedToFilter = resolveAssignedToFilter(assignedTo);
        InquiryType inquiryTypeFilter = parseInquiryTypeNullable(inquiryType);

        return inquiryRepository.findAll().stream()
                .filter(inquiry -> matchesSearch(inquiry, normalizedSearch))
                .filter(inquiry -> statusFilter == null || inquiry.getStatus() == statusFilter)
                .filter(inquiry -> sourceFilter == null || sourceFilter.equalsIgnoreCase(inquiry.getSource()))
                .filter(inquiry -> assignedToFilter == null || containsIgnoreCase(inquiry.getAssignedTo(), assignedToFilter))
                .filter(inquiry -> inquiryTypeFilter == null || resolveInquiryType(inquiry) == inquiryTypeFilter)
                .sorted(Comparator.comparing(Inquiry::getCreatedAt).reversed())
                .map(this::mapInquiry)
                .toList();
    }

    @Transactional(readOnly = true)
    public InquiryResponse getInquiry(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));
        return mapInquiry(inquiry);
    }

    @Transactional
    public InquiryResponse updateInquiry(Long id, UpdateInquiryRequest request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

        VerificationStatus verificationStatus = parseVerificationStatusNullable(request.verificationStatus());
        PaymentStatus paymentStatus = parsePaymentStatusNullable(request.paymentStatus());
        String agreementId = normalizeNullable(request.agreementId());
        InquiryType resolvedType = resolveInquiryType(inquiry);
        if (resolvedType == InquiryType.INVESTOR) {
            if ((paymentStatus == PaymentStatus.RECEIVED || paymentStatus == PaymentStatus.VERIFIED)
                    && paymentStatus != inquiry.getPaymentStatus()) {
                throw new IllegalArgumentException("Investor payment status is updated only by the verified payment workflow");
            }
            if (agreementId != null && !agreementId.equals(inquiry.getAgreementId())) {
                throw new IllegalArgumentException("Investor agreement ID is managed by the investor onboarding workflow");
            }
            if (request.committedReturnAmount() != null
                    && request.committedReturnAmount().compareTo(inquiry.getCommittedReturnAmount() == null
                            ? BigDecimal.ZERO : inquiry.getCommittedReturnAmount()) != 0) {
                throw new IllegalArgumentException("Investor committed return is managed by the investor onboarding workflow");
            }
        }
        if (agreementId == null
                && resolvedType == InquiryType.INVESTOR
                && (paymentStatus == PaymentStatus.RECEIVED || paymentStatus == PaymentStatus.VERIFIED)
                && inquiry.getAgreementId() == null) {
            agreementId = generateAgreementId();
        }

        inquiry.updateStatus(
                parseStatus(request.status()),
                verificationStatus,
                paymentStatus,
                normalizeNullable(request.adminNotes()),
                normalizeAssignedToForActor(request.assignedTo()),
                agreementId,
                request.committedReturnAmount(),
                normalizeNullable(request.farmerActionNote()),
                normalizeNullable(request.hubActionNote())
        );
        return mapInquiry(inquiryRepository.save(inquiry));
    }

    @Transactional
    public InquiryResponse convertToLead(Long inquiryId, ConvertInquiryToLeadRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

        if (resolveInquiryType(inquiry) == InquiryType.INVESTOR) {
            throw new IllegalArgumentException("Investor inquiries use the approval and payment onboarding workflow");
        }

        if (inquiry.getConvertedLeadId() != null || inquiry.getStatus() == InquiryStatus.CONVERTED) {
            throw new IllegalArgumentException("Inquiry already converted");
        }

        String leadNotes = normalizeNullable(request.leadNotes());
        String assignedTo = normalizeAssignedToForActor(request.assignedTo());

        LeadResponse lead = leadService.createLeadFromInquiry(
                inquiry.getFullName(),
                inquiry.getEmail(),
                inquiry.getPhone(),
                inquiry.getCompanyName(),
                leadNotes != null ? leadNotes : inquiry.getMessage(),
                assignedTo,
                inquiry.getId()
        );

        inquiry.markConverted(
                lead.id(),
                leadNotes != null ? leadNotes : "Converted to lead",
                assignedTo
        );
        return mapInquiry(inquiryRepository.save(inquiry));
    }

    private InquiryResponse mapInquiry(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                resolveInquiryType(inquiry).name(),
                inquiry.getReferenceId(),
                inquiry.getFullName(),
                inquiry.getCompanyName(),
                inquiry.getEmail(),
                inquiry.getPhone(),
                inquiry.getAlternatePhone(),
                inquiry.getFatherName(),
                inquiry.getAadhaarNumber(),
                inquiry.getPanNumber(),
                inquiry.getFullAddress(),
                inquiry.getProductName(),
                inquiry.getMessage(),
                inquiry.getInvestmentAmount(),
                inquiry.getInvestmentDate(),
                inquiry.getPreferredPaymentMode(),
                inquiry.getTransactionId(),
                inquiry.getPaymentDate(),
                inquiry.getFarmingType(),
                inquiry.getLandArea(),
                inquiry.getMainCrops(),
                inquiry.getIrrigationType(),
                inquiry.getBankAccountNumber(),
                inquiry.getIfscCode(),
                inquiry.getVillage(),
                inquiry.getDistrict(),
                inquiry.getFarmerState(),
                inquiry.getPinCode(),
                inquiry.getCollectionHubName(),
                inquiry.getHubStorageType(),
                inquiry.getHubCapacityMt(),
                inquiry.getHubPickupRadiusKm(),
                inquiry.getHubOperatingDays(),
                inquiry.getHubCode(),
                inquiry.getIdProofUrl(),
                inquiry.getIdProofDocumentId(),
                inquiry.getIdProofMetadata(),
                inquiry.getPaymentScreenshotUrl(),
                inquiry.getPaymentScreenshotDocumentId(),
                inquiry.getPaymentScreenshotMetadata(),
                inquiry.getAadhaarDocumentUrl(),
                inquiry.getAadhaarDocumentId(),
                inquiry.getAadhaarDocumentMetadata(),
                inquiry.getLandProofDocumentUrl(),
                inquiry.getLandProofDocumentId(),
                inquiry.getLandProofDocumentMetadata(),
                inquiry.getBankPassbookDocumentUrl(),
                inquiry.getBankPassbookDocumentId(),
                inquiry.getBankPassbookDocumentMetadata(),
                inquiry.getHubDocumentUrl(),
                inquiry.getHubDocumentId(),
                inquiry.getHubDocumentMetadata(),
                inquiry.isTermsAccepted(),
                inquiry.getAgreementId(),
                inquiry.getCommittedReturnAmount(),
                inquiry.getFarmerActionNote(),
                inquiry.getHubActionNote(),
                resolveVerificationStatus(inquiry).name(),
                resolvePaymentStatus(inquiry).name(),
                inquiry.getSource(),
                inquiry.getAdminNotes(),
                inquiry.getAssignedTo(),
                inquiry.getConvertedLeadId(),
                inquiry.getStatus().name(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }

    private InquiryStatus parseStatus(String value) {
        try {
            return InquiryStatus.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid inquiry status");
        }
    }

    private InquiryStatus parseStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseStatus(value);
    }

    private VerificationStatus parseVerificationStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return VerificationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid verification status");
        }
    }

    private PaymentStatus parsePaymentStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PaymentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid payment status");
        }
    }

    private InquiryType parseInquiryTypeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return InquiryType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid inquiry type filter");
        }
    }

    private boolean matchesSearch(Inquiry inquiry, String search) {
        if (search == null) {
            return true;
        }
        return containsIgnoreCase(inquiry.getFullName(), search)
                || containsIgnoreCase(inquiry.getCompanyName(), search)
                || containsIgnoreCase(inquiry.getEmail(), search)
                || containsIgnoreCase(inquiry.getPhone(), search)
                || containsIgnoreCase(inquiry.getProductName(), search)
                || containsIgnoreCase(inquiry.getMessage(), search)
                || containsIgnoreCase(inquiry.getReferenceId(), search)
                || containsIgnoreCase(inquiry.getTransactionId(), search)
                || containsIgnoreCase(inquiry.getAgreementId(), search)
                || containsIgnoreCase(inquiry.getCollectionHubName(), search)
                || containsIgnoreCase(inquiry.getHubCode(), search)
                || containsIgnoreCase(resolveInquiryType(inquiry).name(), search);
    }

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String normalizeUpperNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String buildDocumentMetadataJson(InquiryDocumentStorageService.StoredDocument storedDocument) {
        if (storedDocument == null) {
            return null;
        }
        return "{"
                + "\"documentId\":\"" + escapeJson(storedDocument.documentId()) + "\","
                + "\"originalFileName\":\"" + escapeJson(storedDocument.originalFileName()) + "\","
                + "\"contentType\":\"" + escapeJson(storedDocument.contentType()) + "\","
                + "\"sizeBytes\":" + (storedDocument.sizeBytes() == null ? "null" : storedDocument.sizeBytes())
                + "}";
    }

    private UUID resolveDocumentId(InquiryDocumentStorageService.StoredDocument storedDocument) {
        if (storedDocument == null || storedDocument.documentId() == null || storedDocument.documentId().isBlank()) {
            return null;
        }
        return UUID.fromString(storedDocument.documentId());
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private String resolveAssignedToFilter(String assignedTo) {
        if ("SALES".equals(getCurrentRole())) {
            return normalizeSearch(getCurrentUsername());
        }
        return normalizeSearch(assignedTo);
    }

    private String normalizeAssignedToForActor(String assignedTo) {
        if ("SALES".equals(getCurrentRole())) {
            return normalizeNullable(getCurrentUsername());
        }
        return normalizeNullable(assignedTo);
    }

    private String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("");
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "";
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(fieldName + " must be in YYYY-MM-DD format");
        }
    }

    private LocalDate parseDateNullable(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseDate(value, fieldName);
    }

    private String generateReferenceId(InquiryType inquiryType) {
        String prefix = switch (inquiryType) {
            case GENERAL -> "INQ";
            case INVESTOR -> "INV";
            case FARMER -> "FAR";
            case COLLECTION_HUB -> "HUB";
        };

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT));
        String referenceId;
        do {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
            referenceId = "FVP-" + prefix + "-" + datePrefix + "-" + suffix;
        } while (inquiryRepository.existsByReferenceId(referenceId));
        return referenceId;
    }

    private String generateAgreementId() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT));
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "AGR-" + datePrefix + "-" + suffix;
    }

    private String generateHubCode() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT));
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "HUB-" + datePrefix + "-" + suffix;
    }

    private InquiryType resolveInquiryType(Inquiry inquiry) {
        return inquiry.getInquiryType() == null ? InquiryType.GENERAL : inquiry.getInquiryType();
    }

    private VerificationStatus resolveVerificationStatus(Inquiry inquiry) {
        return inquiry.getVerificationStatus() == null ? VerificationStatus.PENDING : inquiry.getVerificationStatus();
    }

    private PaymentStatus resolvePaymentStatus(Inquiry inquiry) {
        return inquiry.getPaymentStatus() == null ? PaymentStatus.NOT_REQUIRED : inquiry.getPaymentStatus();
    }
}

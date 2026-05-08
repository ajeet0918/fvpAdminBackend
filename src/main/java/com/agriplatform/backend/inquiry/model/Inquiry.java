package com.agriplatform.backend.inquiry.model;

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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InquiryType inquiryType;

    @Column(length = 40, unique = true)
    private String referenceId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(length = 40)
    private String alternatePhone;

    @Column(length = 120)
    private String fatherName;

    @Column(length = 40)
    private String aadhaarNumber;

    @Column(length = 20)
    private String panNumber;

    @Column(length = 500)
    private String fullAddress;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, length = 1200)
    private String message;

    @Column(precision = 14, scale = 2)
    private BigDecimal investmentAmount;

    private LocalDate investmentDate;

    @Column(length = 80)
    private String preferredPaymentMode;

    @Column(length = 120)
    private String transactionId;

    private LocalDate paymentDate;

    @Column(length = 80)
    private String farmingType;

    @Column(length = 80)
    private String landArea;

    @Column(length = 255)
    private String mainCrops;

    @Column(length = 80)
    private String irrigationType;

    @Column(length = 40)
    private String bankAccountNumber;

    @Column(length = 20)
    private String ifscCode;

    @Column(length = 160)
    private String village;

    @Column(length = 160)
    private String district;

    @Column(length = 120)
    private String farmerState;

    @Column(length = 20)
    private String pinCode;

    @Column(length = 160)
    private String collectionHubName;

    @Column(length = 80)
    private String hubStorageType;

    @Column(precision = 12, scale = 2)
    private BigDecimal hubCapacityMt;

    private Integer hubPickupRadiusKm;

    @Column(length = 120)
    private String hubOperatingDays;

    @Column(length = 40)
    private String hubCode;

    @Column(length = 500)
    private String idProofUrl;

    @Column(length = 1200)
    private String idProofMetadata;

    @Column(length = 500)
    private String paymentScreenshotUrl;

    @Column(length = 1200)
    private String paymentScreenshotMetadata;

    @Column(length = 500)
    private String aadhaarDocumentUrl;

    @Column(length = 1200)
    private String aadhaarDocumentMetadata;

    @Column(length = 500)
    private String landProofDocumentUrl;

    @Column(length = 1200)
    private String landProofDocumentMetadata;

    @Column(length = 500)
    private String bankPassbookDocumentUrl;

    @Column(length = 1200)
    private String bankPassbookDocumentMetadata;

    @Column(length = 500)
    private String hubDocumentUrl;

    @Column(length = 1200)
    private String hubDocumentMetadata;

    private Boolean termsAccepted;

    @Column(length = 40)
    private String agreementId;

    @Column(precision = 14, scale = 2)
    private BigDecimal committedReturnAmount;

    @Column(length = 1200)
    private String farmerActionNote;

    @Column(length = 1200)
    private String hubActionNote;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(length = 1200)
    private String adminNotes;

    @Column(length = 120)
    private String assignedTo;

    private Long convertedLeadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Inquiry() {
    }

    public Inquiry(String fullName, String companyName, String email, String phone, String productName, String message) {
        this(null, fullName, companyName, email, phone, productName, message);
    }

    public Inquiry(
            String referenceId,
            String fullName,
            String companyName,
            String email,
            String phone,
            String productName,
            String message
    ) {
        this.referenceId = referenceId;
        this.inquiryType = InquiryType.GENERAL;
        this.fullName = fullName;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.productName = productName;
        this.message = message;
        this.source = "WEBSITE_INQUIRY";
        this.status = InquiryStatus.NEW;
        this.verificationStatus = VerificationStatus.PENDING;
        this.paymentStatus = PaymentStatus.NOT_REQUIRED;
        this.termsAccepted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Inquiry createInvestor(
            String referenceId,
            String fullName,
            String fatherName,
            String email,
            String phone,
            String aadhaarNumber,
            String panNumber,
            String fullAddress,
            BigDecimal investmentAmount,
            LocalDate investmentDate,
            String preferredPaymentMode,
            String transactionId,
            LocalDate paymentDate,
            String source,
            String message,
            String idProofUrl,
            String idProofMetadata,
            String paymentScreenshotUrl,
            String paymentScreenshotMetadata,
            boolean termsAccepted,
            String agreementId
    ) {
        Inquiry inquiry = new Inquiry(
                referenceId,
                fullName,
                "INDIVIDUAL_INVESTOR",
                email,
                phone,
                "INVESTMENT_PROGRAM",
                message
        );
        inquiry.inquiryType = InquiryType.INVESTOR;
        inquiry.fatherName = fatherName;
        inquiry.aadhaarNumber = aadhaarNumber;
        inquiry.panNumber = panNumber;
        inquiry.fullAddress = fullAddress;
        inquiry.investmentAmount = investmentAmount;
        inquiry.investmentDate = investmentDate;
        inquiry.preferredPaymentMode = preferredPaymentMode;
        inquiry.transactionId = transactionId;
        inquiry.paymentDate = paymentDate;
        inquiry.source = source;
        inquiry.idProofUrl = idProofUrl;
        inquiry.idProofMetadata = idProofMetadata;
        inquiry.paymentScreenshotUrl = paymentScreenshotUrl;
        inquiry.paymentScreenshotMetadata = paymentScreenshotMetadata;
        inquiry.termsAccepted = termsAccepted;
        inquiry.agreementId = agreementId;
        inquiry.paymentStatus = transactionId == null && paymentScreenshotUrl == null
                ? PaymentStatus.PENDING
                : PaymentStatus.RECEIVED;
        return inquiry;
    }

    public static Inquiry createFarmer(
            String referenceId,
            String fullName,
            String fatherName,
            String email,
            String phone,
            String alternatePhone,
            String aadhaarNumber,
            String fullAddress,
            String village,
            String district,
            String farmerState,
            String pinCode,
            String farmingType,
            String landArea,
            String mainCrops,
            String irrigationType,
            String bankAccountNumber,
            String ifscCode,
            String source,
            String message,
            String aadhaarDocumentUrl,
            String aadhaarDocumentMetadata,
            String landProofDocumentUrl,
            String landProofDocumentMetadata,
            String bankPassbookDocumentUrl,
            String bankPassbookDocumentMetadata,
            boolean termsAccepted
    ) {
        Inquiry inquiry = new Inquiry(
                referenceId,
                fullName,
                "FARMER_REGISTRATION",
                email,
                phone,
                "FARMER_ONBOARDING",
                message
        );
        inquiry.inquiryType = InquiryType.FARMER;
        inquiry.fatherName = fatherName;
        inquiry.alternatePhone = alternatePhone;
        inquiry.aadhaarNumber = aadhaarNumber;
        inquiry.fullAddress = fullAddress;
        inquiry.village = village;
        inquiry.district = district;
        inquiry.farmerState = farmerState;
        inquiry.pinCode = pinCode;
        inquiry.farmingType = farmingType;
        inquiry.landArea = landArea;
        inquiry.mainCrops = mainCrops;
        inquiry.irrigationType = irrigationType;
        inquiry.bankAccountNumber = bankAccountNumber;
        inquiry.ifscCode = ifscCode;
        inquiry.source = source;
        inquiry.message = message;
        inquiry.aadhaarDocumentUrl = aadhaarDocumentUrl;
        inquiry.aadhaarDocumentMetadata = aadhaarDocumentMetadata;
        inquiry.landProofDocumentUrl = landProofDocumentUrl;
        inquiry.landProofDocumentMetadata = landProofDocumentMetadata;
        inquiry.bankPassbookDocumentUrl = bankPassbookDocumentUrl;
        inquiry.bankPassbookDocumentMetadata = bankPassbookDocumentMetadata;
        inquiry.termsAccepted = termsAccepted;
        inquiry.paymentStatus = PaymentStatus.NOT_REQUIRED;
        return inquiry;
    }

    public static Inquiry createCollectionHub(
            String referenceId,
            String fullName,
            String fatherName,
            String email,
            String phone,
            String alternatePhone,
            String aadhaarNumber,
            String fullAddress,
            String village,
            String district,
            String farmerState,
            String pinCode,
            String collectionHubName,
            String hubStorageType,
            BigDecimal hubCapacityMt,
            Integer hubPickupRadiusKm,
            String hubOperatingDays,
            String source,
            String message,
            String hubDocumentUrl,
            String hubDocumentMetadata,
            boolean termsAccepted,
            String hubCode
    ) {
        Inquiry inquiry = new Inquiry(
                referenceId,
                fullName,
                "COLLECTION_HUB_PARTNER",
                email,
                phone,
                "COLLECTION_HUB_ONBOARDING",
                message
        );
        inquiry.inquiryType = InquiryType.COLLECTION_HUB;
        inquiry.fatherName = fatherName;
        inquiry.alternatePhone = alternatePhone;
        inquiry.aadhaarNumber = aadhaarNumber;
        inquiry.fullAddress = fullAddress;
        inquiry.village = village;
        inquiry.district = district;
        inquiry.farmerState = farmerState;
        inquiry.pinCode = pinCode;
        inquiry.collectionHubName = collectionHubName;
        inquiry.hubStorageType = hubStorageType;
        inquiry.hubCapacityMt = hubCapacityMt;
        inquiry.hubPickupRadiusKm = hubPickupRadiusKm;
        inquiry.hubOperatingDays = hubOperatingDays;
        inquiry.source = source;
        inquiry.message = message;
        inquiry.hubDocumentUrl = hubDocumentUrl;
        inquiry.hubDocumentMetadata = hubDocumentMetadata;
        inquiry.termsAccepted = termsAccepted;
        inquiry.hubCode = hubCode;
        inquiry.paymentStatus = PaymentStatus.NOT_REQUIRED;
        return inquiry;
    }

    public Long getId() {
        return id;
    }

    public InquiryType getInquiryType() {
        return inquiryType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public String getFatherName() {
        return fatherName;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public String getProductName() {
        return productName;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getInvestmentAmount() {
        return investmentAmount;
    }

    public LocalDate getInvestmentDate() {
        return investmentDate;
    }

    public String getPreferredPaymentMode() {
        return preferredPaymentMode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getFarmingType() {
        return farmingType;
    }

    public String getLandArea() {
        return landArea;
    }

    public String getMainCrops() {
        return mainCrops;
    }

    public String getIrrigationType() {
        return irrigationType;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getVillage() {
        return village;
    }

    public String getDistrict() {
        return district;
    }

    public String getFarmerState() {
        return farmerState;
    }

    public String getPinCode() {
        return pinCode;
    }

    public String getCollectionHubName() {
        return collectionHubName;
    }

    public String getHubStorageType() {
        return hubStorageType;
    }

    public BigDecimal getHubCapacityMt() {
        return hubCapacityMt;
    }

    public Integer getHubPickupRadiusKm() {
        return hubPickupRadiusKm;
    }

    public String getHubOperatingDays() {
        return hubOperatingDays;
    }

    public String getHubCode() {
        return hubCode;
    }

    public String getIdProofUrl() {
        return idProofUrl;
    }

    public String getIdProofMetadata() {
        return idProofMetadata;
    }

    public String getPaymentScreenshotUrl() {
        return paymentScreenshotUrl;
    }

    public String getPaymentScreenshotMetadata() {
        return paymentScreenshotMetadata;
    }

    public String getAadhaarDocumentUrl() {
        return aadhaarDocumentUrl;
    }

    public String getAadhaarDocumentMetadata() {
        return aadhaarDocumentMetadata;
    }

    public String getLandProofDocumentUrl() {
        return landProofDocumentUrl;
    }

    public String getLandProofDocumentMetadata() {
        return landProofDocumentMetadata;
    }

    public String getBankPassbookDocumentUrl() {
        return bankPassbookDocumentUrl;
    }

    public String getBankPassbookDocumentMetadata() {
        return bankPassbookDocumentMetadata;
    }

    public String getHubDocumentUrl() {
        return hubDocumentUrl;
    }

    public String getHubDocumentMetadata() {
        return hubDocumentMetadata;
    }

    public boolean isTermsAccepted() {
        return Boolean.TRUE.equals(termsAccepted);
    }

    public String getAgreementId() {
        return agreementId;
    }

    public BigDecimal getCommittedReturnAmount() {
        return committedReturnAmount;
    }

    public String getFarmerActionNote() {
        return farmerActionNote;
    }

    public String getHubActionNote() {
        return hubActionNote;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getSource() {
        return source;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Long getConvertedLeadId() {
        return convertedLeadId;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateStatus(
            InquiryStatus status,
            VerificationStatus verificationStatus,
            PaymentStatus paymentStatus,
            String adminNotes,
            String assignedTo,
            String agreementId,
            BigDecimal committedReturnAmount,
            String farmerActionNote,
            String hubActionNote
    ) {
        this.status = status;
        if (verificationStatus != null) {
            this.verificationStatus = verificationStatus;
        }
        if (paymentStatus != null) {
            this.paymentStatus = paymentStatus;
        }
        this.adminNotes = adminNotes;
        this.assignedTo = assignedTo;
        if (agreementId != null && !agreementId.isBlank()) {
            this.agreementId = agreementId.trim();
        }
        if (committedReturnAmount != null) {
            this.committedReturnAmount = committedReturnAmount;
        }
        if (farmerActionNote != null) {
            this.farmerActionNote = farmerActionNote;
        }
        if (hubActionNote != null) {
            this.hubActionNote = hubActionNote;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void markConverted(Long leadId, String adminNotes, String assignedTo) {
        this.status = InquiryStatus.CONVERTED;
        this.convertedLeadId = leadId;
        this.adminNotes = adminNotes;
        this.assignedTo = assignedTo;
        this.updatedAt = LocalDateTime.now();
    }
}

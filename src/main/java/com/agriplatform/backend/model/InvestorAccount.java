package com.agriplatform.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_account")
public class InvestorAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String investorCode;

    @Column(nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 40)
    private String phone;

    private Long sourceInquiryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestorAccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    @Column(length = 1200)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public InvestorAccount() {
    }

    public InvestorAccount(
            String investorCode,
            String fullName,
            String email,
            String phone,
            Long sourceInquiryId,
            InvestorAccountStatus status,
            VerificationStatus verificationStatus,
            String notes
    ) {
        this.investorCode = investorCode;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.sourceInquiryId = sourceInquiryId;
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getInvestorCode() {
        return investorCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getSourceInquiryId() {
        return sourceInquiryId;
    }

    public InvestorAccountStatus getStatus() {
        return status;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateProfile(
            String fullName,
            String email,
            String phone,
            InvestorAccountStatus status,
            VerificationStatus verificationStatus,
            String notes
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.verificationStatus = verificationStatus;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
}

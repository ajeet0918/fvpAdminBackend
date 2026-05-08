package com.agriplatform.backend.investor.model;

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

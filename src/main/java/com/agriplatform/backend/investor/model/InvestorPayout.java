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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_payout")
public class InvestorPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_account_id")
    private InvestorAccount investorAccount;

    @Column(nullable = false, unique = true, length = 40)
    private String payoutReference;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestorPayoutStatus status;

    @Column(length = 80)
    private String paymentChannel;

    @Column(length = 120)
    private String transactionReference;

    @Column(length = 1200)
    private String notes;

    @Column(length = 120)
    private String approvedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime paidAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public InvestorPayout() {
    }

    public InvestorPayout(
            InvestorAccount investorAccount,
            String payoutReference,
            BigDecimal totalAmount,
            InvestorPayoutStatus status,
            String notes
    ) {
        this.investorAccount = investorAccount;
        this.payoutReference = payoutReference;
        this.totalAmount = totalAmount;
        this.status = status;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public InvestorAccount getInvestorAccount() {
        return investorAccount;
    }

    public String getPayoutReference() {
        return payoutReference;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public InvestorPayoutStatus getStatus() {
        return status;
    }

    public String getPaymentChannel() {
        return paymentChannel;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getNotes() {
        return notes;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void approve(String approvedBy, String notes) {
        this.status = InvestorPayoutStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        if (notes != null && !notes.isBlank()) {
            this.notes = notes.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String notes) {
        this.status = InvestorPayoutStatus.REJECTED;
        if (notes != null && !notes.isBlank()) {
            this.notes = notes.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaid(String paymentChannel, String transactionReference, LocalDateTime paidAt, String notes) {
        this.status = InvestorPayoutStatus.PAID;
        this.paymentChannel = paymentChannel;
        this.transactionReference = transactionReference;
        this.paidAt = paidAt == null ? LocalDateTime.now() : paidAt;
        if (notes != null && !notes.isBlank()) {
            this.notes = notes.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }
}

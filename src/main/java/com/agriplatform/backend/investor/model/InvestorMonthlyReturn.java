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
@Table(name = "investor_monthly_return")
public class InvestorMonthlyReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_account_id")
    private InvestorAccount investorAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id")
    private Investment investment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id")
    private InvestorPayout payout;

    @Column(nullable = false)
    private Integer periodYear;

    @Column(nullable = false)
    private Integer periodMonth;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal basePrincipal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal returnRate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal calculatedAmount;

    @Column(precision = 14, scale = 2)
    private BigDecimal overrideAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal finalAmount;

    @Column(length = 300)
    private String overrideReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestorMonthlyReturnStatus status;

    @Column(length = 120)
    private String submittedBy;

    private LocalDateTime submittedAt;

    @Column(length = 120)
    private String approvedBy;

    private LocalDateTime approvedAt;

    @Column(length = 1200)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public InvestorMonthlyReturn() {
    }

    public InvestorMonthlyReturn(
            InvestorAccount investorAccount,
            Investment investment,
            Integer periodYear,
            Integer periodMonth,
            BigDecimal basePrincipal,
            BigDecimal returnRate,
            BigDecimal calculatedAmount
    ) {
        this.investorAccount = investorAccount;
        this.investment = investment;
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
        this.basePrincipal = basePrincipal;
        this.returnRate = returnRate;
        this.calculatedAmount = calculatedAmount;
        this.finalAmount = calculatedAmount;
        this.status = InvestorMonthlyReturnStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public InvestorAccount getInvestorAccount() {
        return investorAccount;
    }

    public Investment getInvestment() {
        return investment;
    }

    public InvestorPayout getPayout() {
        return payout;
    }

    public Integer getPeriodYear() {
        return periodYear;
    }

    public Integer getPeriodMonth() {
        return periodMonth;
    }

    public BigDecimal getBasePrincipal() {
        return basePrincipal;
    }

    public BigDecimal getReturnRate() {
        return returnRate;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    public BigDecimal getOverrideAmount() {
        return overrideAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public InvestorMonthlyReturnStatus getStatus() {
        return status;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
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

    public void applyOverride(BigDecimal overrideAmount, String overrideReason, String notes) {
        this.overrideAmount = overrideAmount;
        this.overrideReason = overrideReason;
        this.finalAmount = overrideAmount == null ? this.calculatedAmount : overrideAmount;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void submit(String submittedBy, String notes) {
        this.status = InvestorMonthlyReturnStatus.SUBMITTED;
        this.submittedBy = submittedBy;
        this.submittedAt = LocalDateTime.now();
        if (notes != null && !notes.isBlank()) {
            this.notes = notes.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(String approvedBy, String notes) {
        this.status = InvestorMonthlyReturnStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        if (notes != null && !notes.isBlank()) {
            this.notes = notes.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String notes) {
        this.status = InvestorMonthlyReturnStatus.REJECTED;
        if (notes != null && !notes.isBlank()) {
            this.notes = notes.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void hold(String notes) {
        this.status = InvestorMonthlyReturnStatus.HOLD;
        if (notes != null && !notes.isBlank()) {
            this.notes = notes.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaid(InvestorPayout payout) {
        this.status = InvestorMonthlyReturnStatus.PAID;
        this.payout = payout;
        this.updatedAt = LocalDateTime.now();
    }

    public void attachPayout(InvestorPayout payout) {
        this.payout = payout;
        this.updatedAt = LocalDateTime.now();
    }

    public void detachPayout() {
        this.payout = null;
        this.updatedAt = LocalDateTime.now();
    }
}

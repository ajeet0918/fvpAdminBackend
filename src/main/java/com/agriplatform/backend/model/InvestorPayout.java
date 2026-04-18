package com.agriplatform.backend.model;

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

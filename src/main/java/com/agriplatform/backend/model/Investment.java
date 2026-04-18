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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_account_id")
    private InvestorAccount investorAccount;

    @Column(nullable = false, unique = true, length = 40)
    private String investmentReference;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal monthlyReturnRate;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvestmentStatus status;

    @Column(length = 1200)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Investment() {
    }

    public Investment(
            InvestorAccount investorAccount,
            String investmentReference,
            BigDecimal principalAmount,
            BigDecimal monthlyReturnRate,
            LocalDate startDate,
            LocalDate endDate,
            InvestmentStatus status,
            String notes
    ) {
        this.investorAccount = investorAccount;
        this.investmentReference = investmentReference;
        this.principalAmount = principalAmount;
        this.monthlyReturnRate = monthlyReturnRate;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getInvestmentReference() {
        return investmentReference;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getMonthlyReturnRate() {
        return monthlyReturnRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public InvestmentStatus getStatus() {
        return status;
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

    public void update(
            BigDecimal principalAmount,
            BigDecimal monthlyReturnRate,
            LocalDate startDate,
            LocalDate endDate,
            InvestmentStatus status,
            String notes
    ) {
        this.principalAmount = principalAmount;
        this.monthlyReturnRate = monthlyReturnRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
}

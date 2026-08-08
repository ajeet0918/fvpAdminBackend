package com.agriplatform.backend.investor.model;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_agreement")
public class InvestorAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_account_id", nullable = false)
    private InvestorAccount investorAccount;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", nullable = false, unique = true)
    private Investment investment;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_payment_id", nullable = false, unique = true)
    private InvestorPayment investorPayment;

    @Column(nullable = false, unique = true, length = 80)
    private String agreementNumber;

    @Column(nullable = false, length = 80)
    private String termsVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String termsText;

    @Column(nullable = false, length = 240)
    private String companyLegalName;

    @Column(nullable = false, length = 1000)
    private String companyAddress;

    @Column(nullable = false, length = 160)
    private String authorizedSignatory;

    @Column(nullable = false, length = 160)
    private String investorName;

    @Column(nullable = false, length = 1000)
    private String investorAddress;

    @Column(nullable = false, length = 20)
    private String panMasked;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal monthlyReturnRate;

    @Column(nullable = false)
    private LocalDate investmentStartDate;

    private LocalDate investmentEndDate;

    @Column(length = 160)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestorAgreementStatus status;

    @Column(length = 600)
    private String generationError;

    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected InvestorAgreement() {
    }

    public InvestorAgreement(
            InvestorAccount investorAccount,
            Investment investment,
            InvestorPayment investorPayment,
            String agreementNumber,
            String termsVersion,
            String termsText,
            String companyLegalName,
            String companyAddress,
            String authorizedSignatory,
            String investorName,
            String investorAddress,
            String panMasked
    ) {
        this.investorAccount = investorAccount;
        this.investment = investment;
        this.investorPayment = investorPayment;
        this.agreementNumber = agreementNumber;
        this.termsVersion = termsVersion;
        this.termsText = termsText;
        this.companyLegalName = companyLegalName;
        this.companyAddress = companyAddress;
        this.authorizedSignatory = authorizedSignatory;
        this.investorName = investorName;
        this.investorAddress = investorAddress;
        this.panMasked = panMasked;
        this.principalAmount = investment.getPrincipalAmount();
        this.monthlyReturnRate = investment.getMonthlyReturnRate();
        this.investmentStartDate = investment.getStartDate();
        this.investmentEndDate = investment.getEndDate();
        this.status = InvestorAgreementStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public InvestorAccount getInvestorAccount() { return investorAccount; }
    public Investment getInvestment() { return investment; }
    public InvestorPayment getInvestorPayment() { return investorPayment; }
    public String getAgreementNumber() { return agreementNumber; }
    public String getTermsVersion() { return termsVersion; }
    public String getTermsText() { return termsText; }
    public String getCompanyLegalName() { return companyLegalName; }
    public String getCompanyAddress() { return companyAddress; }
    public String getAuthorizedSignatory() { return authorizedSignatory; }
    public String getInvestorName() { return investorName; }
    public String getInvestorAddress() { return investorAddress; }
    public String getPanMasked() { return panMasked; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public BigDecimal getMonthlyReturnRate() { return monthlyReturnRate; }
    public LocalDate getInvestmentStartDate() { return investmentStartDate; }
    public LocalDate getInvestmentEndDate() { return investmentEndDate; }
    public String getPaymentReference() { return paymentReference; }
    public InvestorAgreementStatus getStatus() { return status; }
    public String getGenerationError() { return generationError; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    public void markAvailable(String paymentReference) {
        this.paymentReference = paymentReference;
        this.status = InvestorAgreementStatus.AVAILABLE;
        this.generationError = null;
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = this.generatedAt;
    }

    public void markFailed(String message) {
        this.status = InvestorAgreementStatus.FAILED;
        String normalized = message == null || message.isBlank() ? "Agreement finalization failed" : message.trim();
        this.generationError = normalized.length() <= 600 ? normalized : normalized.substring(0, 600);
        this.updatedAt = LocalDateTime.now();
    }
}

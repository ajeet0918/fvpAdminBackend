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
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_payment")
public class InvestorPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_account_id", nullable = false)
    private InvestorAccount investorAccount;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", nullable = false, unique = true)
    private Investment investment;

    @Column(nullable = false, unique = true)
    private Long sourceInquiryId;

    @Column(nullable = false, unique = true, length = 80)
    private String merchantLinkId;

    @Column(length = 120)
    private String providerLinkId;

    @Column(length = 1200)
    private String linkUrl;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 8)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestorPaymentStatus status;

    private LocalDateTime linkExpiresAt;

    @Column(length = 160)
    private String paymentReference;

    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestorDeliveryStatus emailDeliveryStatus;

    private LocalDateTime emailSentAt;

    @Column(length = 600)
    private String emailError;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvestorDeliveryStatus portalInviteStatus;

    private LocalDateTime portalInviteSentAt;

    @Column(length = 600)
    private String portalInviteError;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected InvestorPayment() {
    }

    public InvestorPayment(
            InvestorAccount investorAccount,
            Investment investment,
            Long sourceInquiryId,
            String merchantLinkId,
            String providerLinkId,
            String linkUrl,
            BigDecimal amount,
            String currency,
            LocalDateTime linkExpiresAt
    ) {
        this.investorAccount = investorAccount;
        this.investment = investment;
        this.sourceInquiryId = sourceInquiryId;
        this.merchantLinkId = merchantLinkId;
        this.providerLinkId = providerLinkId;
        this.linkUrl = linkUrl;
        this.amount = amount;
        this.amountPaid = BigDecimal.ZERO.setScale(2);
        this.currency = currency;
        this.status = InvestorPaymentStatus.LINK_CREATED;
        this.linkExpiresAt = linkExpiresAt;
        this.emailDeliveryStatus = InvestorDeliveryStatus.PENDING;
        this.portalInviteStatus = InvestorDeliveryStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public InvestorAccount getInvestorAccount() { return investorAccount; }
    public Investment getInvestment() { return investment; }
    public Long getSourceInquiryId() { return sourceInquiryId; }
    public String getMerchantLinkId() { return merchantLinkId; }
    public String getProviderLinkId() { return providerLinkId; }
    public String getLinkUrl() { return linkUrl; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public String getCurrency() { return currency; }
    public InvestorPaymentStatus getStatus() { return status; }
    public LocalDateTime getLinkExpiresAt() { return linkExpiresAt; }
    public String getPaymentReference() { return paymentReference; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public InvestorDeliveryStatus getEmailDeliveryStatus() { return emailDeliveryStatus; }
    public LocalDateTime getEmailSentAt() { return emailSentAt; }
    public String getEmailError() { return emailError; }
    public InvestorDeliveryStatus getPortalInviteStatus() { return portalInviteStatus; }
    public LocalDateTime getPortalInviteSentAt() { return portalInviteSentAt; }
    public String getPortalInviteError() { return portalInviteError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void markEmailSent() {
        this.emailDeliveryStatus = InvestorDeliveryStatus.SENT;
        this.emailSentAt = LocalDateTime.now();
        this.emailError = null;
        this.updatedAt = this.emailSentAt;
    }

    public void markEmailFailed(String message) {
        this.emailDeliveryStatus = InvestorDeliveryStatus.FAILED;
        this.emailError = truncate(message);
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePayment(
            InvestorPaymentStatus status,
            BigDecimal amountPaid,
            String paymentReference,
            LocalDateTime paidAt
    ) {
        this.status = status;
        if (amountPaid != null) {
            this.amountPaid = amountPaid;
        }
        if (hasText(paymentReference)) {
            this.paymentReference = paymentReference.trim();
        }
        if (paidAt != null) {
            this.paidAt = paidAt;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void markPortalInviteSent() {
        this.portalInviteStatus = InvestorDeliveryStatus.SENT;
        this.portalInviteSentAt = LocalDateTime.now();
        this.portalInviteError = null;
        this.updatedAt = this.portalInviteSentAt;
    }

    public void markPortalInviteFailed(String message) {
        this.portalInviteStatus = InvestorDeliveryStatus.FAILED;
        this.portalInviteError = truncate(message);
        this.updatedAt = LocalDateTime.now();
    }

    private String truncate(String value) {
        if (!hasText(value)) {
            return "Unexpected delivery failure";
        }
        String normalized = value.trim();
        return normalized.length() <= 600 ? normalized : normalized.substring(0, 600);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

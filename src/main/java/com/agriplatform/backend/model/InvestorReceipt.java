package com.agriplatform.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_receipt")
public class InvestorReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payout_id")
    private InvestorPayout payout;

    @Column(nullable = false, unique = true, length = 40)
    private String receiptNumber;

    @Column(length = 500)
    private String documentUrl;

    @Column(nullable = false)
    private Integer version;

    @Column(length = 120)
    private String generatedBy;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    public InvestorReceipt() {
    }

    public InvestorReceipt(
            InvestorPayout payout,
            String receiptNumber,
            String documentUrl,
            Integer version,
            String generatedBy
    ) {
        this.payout = payout;
        this.receiptNumber = receiptNumber;
        this.documentUrl = documentUrl;
        this.version = version;
        this.generatedBy = generatedBy;
        this.generatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public InvestorPayout getPayout() {
        return payout;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public Integer getVersion() {
        return version;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}

package com.agriplatform.backend.investor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "investor_payment_event")
public class InvestorPaymentEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investor_payment_id", nullable = false)
    private InvestorPayment investorPayment;

    @Column(nullable = false, unique = true, length = 128)
    private String eventKey;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(length = 40)
    private String linkStatus;

    @Column(precision = 14, scale = 2)
    private BigDecimal amountPaid;

    @Column(length = 160)
    private String paymentReference;

    private LocalDateTime eventTime;

    @Column(length = 4000)
    private String payloadSnapshot;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected InvestorPaymentEvent() {
    }

    public InvestorPaymentEvent(
            InvestorPayment investorPayment,
            String eventKey,
            String eventType,
            String linkStatus,
            BigDecimal amountPaid,
            String paymentReference,
            LocalDateTime eventTime,
            String payloadSnapshot
    ) {
        this.investorPayment = investorPayment;
        this.eventKey = eventKey;
        this.eventType = eventType;
        this.linkStatus = linkStatus;
        this.amountPaid = amountPaid;
        this.paymentReference = paymentReference;
        this.eventTime = eventTime;
        this.payloadSnapshot = payloadSnapshot;
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}

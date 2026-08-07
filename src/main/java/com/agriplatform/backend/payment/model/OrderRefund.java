package com.agriplatform.backend.payment.model;

import com.agriplatform.backend.order.model.PurchaseOrder;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class OrderRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false, unique = true, length = 40)
    private String refundId;

    @Column(unique = true, length = 140)
    private String providerRefundId;

    @Column(length = 140)
    private String providerPaymentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderRefundMethod refundMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderRefundStatus status;

    @Column(nullable = false, length = 20)
    private String speed;

    @Column(nullable = false, length = 100)
    private String note;

    @Column(length = 500)
    private String statusDescription;

    @Column(length = 140)
    private String refundArn;

    @Column(nullable = false, length = 120)
    private String requestedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime processedAt;

    protected OrderRefund() {
    }

    public OrderRefund(
            PurchaseOrder purchaseOrder,
            String refundId,
            BigDecimal amount,
            String currency,
            OrderRefundMethod refundMethod,
            String speed,
            String note,
            String requestedBy
    ) {
        this.purchaseOrder = purchaseOrder;
        this.refundId = refundId;
        this.amount = amount;
        this.currency = currency;
        this.refundMethod = refundMethod;
        this.speed = speed;
        this.note = note;
        this.requestedBy = requestedBy;
        this.status = OrderRefundStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public OrderRefund(
            PurchaseOrder purchaseOrder,
            String refundId,
            BigDecimal amount,
            String currency,
            String speed,
            String note,
            String requestedBy
    ) {
        this(purchaseOrder, refundId, amount, currency, OrderRefundMethod.CASHFREE, speed, note, requestedBy);
    }

    public boolean applyGatewayStatus(
            String providerRefundId,
            String providerPaymentId,
            OrderRefundStatus status,
            String statusDescription,
            String refundArn,
            LocalDateTime processedAt
    ) {
        boolean statusChanged = this.status != status;
        this.providerRefundId = providerRefundId;
        this.providerPaymentId = providerPaymentId;
        this.status = status;
        this.statusDescription = statusDescription;
        this.refundArn = refundArn;
        this.processedAt = processedAt;
        this.updatedAt = LocalDateTime.now();
        return statusChanged;
    }

    public Long getId() {
        return id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public String getRefundId() {
        return refundId;
    }

    public String getProviderRefundId() {
        return providerRefundId;
    }

    public String getProviderPaymentId() {
        return providerPaymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public OrderRefundMethod getRefundMethod() {
        return refundMethod;
    }

    public OrderRefundStatus getStatus() {
        return status;
    }

    public String getSpeed() {
        return speed;
    }

    public String getNote() {
        return note;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public String getRefundArn() {
        return refundArn;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}

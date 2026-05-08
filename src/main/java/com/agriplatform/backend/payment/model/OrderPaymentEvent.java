package com.agriplatform.backend.payment.model;

import com.agriplatform.backend.order.model.PurchaseOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class OrderPaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(length = 120)
    private String providerOrderId;

    @Column(length = 120)
    private String providerReference;

    @Column(length = 40)
    private String status;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected OrderPaymentEvent() {
    }

    public OrderPaymentEvent(
            PurchaseOrder purchaseOrder,
            String eventType,
            String providerOrderId,
            String providerReference,
            String status,
            String payload
    ) {
        this.purchaseOrder = purchaseOrder;
        this.eventType = eventType;
        this.providerOrderId = providerOrderId;
        this.providerReference = providerReference;
        this.status = status;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }
}

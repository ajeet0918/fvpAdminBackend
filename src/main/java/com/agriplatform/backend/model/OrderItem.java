package com.agriplatform.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String productName;

    @Column
    private String productSlug;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private String moqSnapshot;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal lineSubtotal;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount;

    public OrderItem() {
    }

    public OrderItem(
            Product product,
            String productName,
            String productSlug,
            Integer quantity,
            String unit,
            String moqSnapshot,
            BigDecimal taxRate,
            BigDecimal discountRate
    ) {
        this.product = product;
        this.productName = productName;
        this.productSlug = productSlug;
        this.quantity = quantity;
        this.unit = unit;
        this.moqSnapshot = moqSnapshot;
        this.taxRate = taxRate;
        this.discountRate = discountRate;
    }

    public Long getId() {
        return id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getProductName() {
        return productName;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getMoqSnapshot() {
        return moqSnapshot;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public BigDecimal getLineSubtotal() {
        return lineSubtotal;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void applyQuote(
            BigDecimal unitPrice,
            BigDecimal lineSubtotal,
            BigDecimal discountRate,
            BigDecimal discountAmount,
            BigDecimal taxRate,
            BigDecimal taxAmount,
            BigDecimal lineTotal
    ) {
        this.unitPrice = unitPrice;
        this.lineSubtotal = lineSubtotal;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.taxRate = taxRate;
        this.taxAmount = taxAmount;
        this.lineTotal = lineTotal;
    }
}

package com.agriplatform.backend.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, length = 400)
    private String deliveryAddress;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false, length = 1600)
    private String customerNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseOrderStatus status;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime quotedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    @Column(length = 1400)
    private String adminNotes;

    @Column(length = 120)
    private String quoteReference;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal shippingAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal effectiveTaxRate;

    @Column(precision = 5, scale = 2)
    private BigDecimal effectiveDiscountRate;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public PurchaseOrder() {
    }

    public PurchaseOrder(
            String orderNumber,
            String fullName,
            String companyName,
            String email,
            String phone,
            String deliveryAddress,
            String city,
            String state,
            String postalCode,
            String customerNotes
    ) {
        this.orderNumber = orderNumber;
        this.fullName = fullName;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.customerNotes = customerNotes;
        this.status = PurchaseOrderStatus.PENDING_REVIEW;
        this.currency = "INR";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCustomerNotes() {
        return customerNotes;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getQuotedAt() {
        return quotedAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public String getQuoteReference() {
        return quoteReference;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getEffectiveTaxRate() {
        return effectiveTaxRate;
    }

    public BigDecimal getEffectiveDiscountRate() {
        return effectiveDiscountRate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void addItem(OrderItem item) {
        item.setPurchaseOrder(this);
        this.items.add(item);
    }

    public void clearItems() {
        this.items.clear();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void addStatusHistory(OrderStatusHistory history) {
        history.setPurchaseOrder(this);
        this.statusHistory.add(history);
    }

    public void applyPricing(
            String adminNotes,
            BigDecimal subtotalAmount,
            BigDecimal shippingAmount,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal effectiveTaxRate,
            BigDecimal effectiveDiscountRate,
            BigDecimal totalAmount
    ) {
        this.adminNotes = adminNotes;
        this.subtotalAmount = subtotalAmount;
        this.shippingAmount = shippingAmount;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.effectiveTaxRate = effectiveTaxRate;
        this.effectiveDiscountRate = effectiveDiscountRate;
        this.totalAmount = totalAmount;
    }

    public void applyQuote(
            String quoteReference,
            String adminNotes,
            BigDecimal subtotalAmount,
            BigDecimal shippingAmount,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal effectiveTaxRate,
            BigDecimal effectiveDiscountRate,
            BigDecimal totalAmount
    ) {
        this.quoteReference = quoteReference;
        applyPricing(
                adminNotes,
                subtotalAmount,
                shippingAmount,
                taxAmount,
                discountAmount,
                effectiveTaxRate,
                effectiveDiscountRate,
                totalAmount
        );
        if (this.quotedAt == null) {
            this.quotedAt = LocalDateTime.now();
        }
    }

    public void updateStatus(PurchaseOrderStatus status, String adminNotes) {
        this.status = status;
        this.adminNotes = adminNotes;
        LocalDateTime now = LocalDateTime.now();
        if (status == PurchaseOrderStatus.CONFIRMED) {
            this.confirmedAt = now;
        } else if (status == PurchaseOrderStatus.SHIPPED) {
            this.shippedAt = now;
        } else if (status == PurchaseOrderStatus.DELIVERED) {
            this.deliveredAt = now;
        }
    }
}

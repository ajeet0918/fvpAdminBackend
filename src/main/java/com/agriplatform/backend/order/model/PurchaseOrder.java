package com.agriplatform.backend.order.model;

import com.agriplatform.backend.*;
import com.agriplatform.backend.auth.controller.*;
import com.agriplatform.backend.auth.dto.*;
import com.agriplatform.backend.auth.service.*;
import com.agriplatform.backend.category.controller.*;
import com.agriplatform.backend.category.model.*;
import com.agriplatform.backend.category.repository.*;
import com.agriplatform.backend.common.controller.*;
import com.agriplatform.backend.config.*;
import com.agriplatform.backend.customer.controller.*;
import com.agriplatform.backend.customer.dto.*;
import com.agriplatform.backend.customer.model.*;
import com.agriplatform.backend.customer.repository.*;
import com.agriplatform.backend.customer.service.*;
import com.agriplatform.backend.document.controller.*;
import com.agriplatform.backend.document.dto.*;
import com.agriplatform.backend.document.model.*;
import com.agriplatform.backend.document.repository.*;
import com.agriplatform.backend.document.service.*;
import com.agriplatform.backend.inquiry.controller.*;
import com.agriplatform.backend.inquiry.dto.*;
import com.agriplatform.backend.inquiry.model.*;
import com.agriplatform.backend.inquiry.repository.*;
import com.agriplatform.backend.inquiry.service.*;
import com.agriplatform.backend.investor.controller.*;
import com.agriplatform.backend.investor.dto.*;
import com.agriplatform.backend.investor.model.*;
import com.agriplatform.backend.investor.repository.*;
import com.agriplatform.backend.investor.service.*;
import com.agriplatform.backend.lead.controller.*;
import com.agriplatform.backend.lead.dto.*;
import com.agriplatform.backend.lead.model.*;
import com.agriplatform.backend.lead.repository.*;
import com.agriplatform.backend.lead.service.*;
import com.agriplatform.backend.order.controller.*;
import com.agriplatform.backend.order.dto.*;
import com.agriplatform.backend.order.model.*;
import com.agriplatform.backend.order.repository.*;
import com.agriplatform.backend.order.service.*;
import com.agriplatform.backend.portal.controller.*;
import com.agriplatform.backend.portal.dto.*;
import com.agriplatform.backend.portal.model.*;
import com.agriplatform.backend.portal.repository.*;
import com.agriplatform.backend.portal.service.*;
import com.agriplatform.backend.payment.model.OrderRefund;
import com.agriplatform.backend.product.controller.*;
import com.agriplatform.backend.product.dto.*;
import com.agriplatform.backend.product.model.*;
import com.agriplatform.backend.product.repository.*;
import com.agriplatform.backend.product.service.*;
import com.agriplatform.backend.security.*;
import com.agriplatform.backend.user.controller.*;
import com.agriplatform.backend.user.dto.*;
import com.agriplatform.backend.user.model.*;
import com.agriplatform.backend.user.repository.*;
import com.agriplatform.backend.user.service.*;

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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PurchaseOrder.class);

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderPaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderPaymentMethod paymentMethod;

    @Column(precision = 12, scale = 2)
    private BigDecimal paymentDueAmount;

    private LocalDateTime paymentDueAt;

    @Column(length = 80)
    private String paymentProvider;

    @Column(length = 140)
    private String paymentProviderOrderId;

    @Column(length = 140)
    private String paymentProviderReference;

    @Column(length = 120)
    private String paymentCollectedBy;

    @Column(length = 140)
    private String paymentCollectionReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderCancellationStatus cancellationStatus;

    @Column(length = 600)
    private String cancellationReason;

    @Column(length = 120)
    private String cancellationRequestedBy;

    private LocalDateTime cancellationRequestedAt;

    @Column(length = 600)
    private String cancellationDecisionNote;

    private LocalDateTime paidAt;

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

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderRefund> refunds = new ArrayList<>();

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
        this.paymentMethod = OrderPaymentMethod.ONLINE;
        this.paymentStatus = OrderPaymentStatus.NOT_INITIATED;
        this.cancellationStatus = OrderCancellationStatus.NONE;
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

    public OrderPaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public OrderPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getPaymentDueAmount() {
        return paymentDueAmount;
    }

    public LocalDateTime getPaymentDueAt() {
        return paymentDueAt;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getPaymentProviderOrderId() {
        return paymentProviderOrderId;
    }

    public String getPaymentProviderReference() {
        return paymentProviderReference;
    }

    public String getPaymentCollectedBy() {
        return paymentCollectedBy;
    }

    public String getPaymentCollectionReference() {
        return paymentCollectionReference;
    }

    public OrderCancellationStatus getCancellationStatus() {
        return cancellationStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getCancellationRequestedBy() {
        return cancellationRequestedBy;
    }

    public LocalDateTime getCancellationRequestedAt() {
        return cancellationRequestedAt;
    }

    public String getCancellationDecisionNote() {
        return cancellationDecisionNote;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
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

    public void markPaymentPending(String provider, String providerOrderId, BigDecimal dueAmount) {
        this.paymentProvider = provider;
        this.paymentProviderOrderId = providerOrderId;
        this.paymentDueAmount = dueAmount;
        this.paymentStatus = OrderPaymentStatus.PENDING;
    }

    public void configurePaymentMethod(OrderPaymentMethod method, BigDecimal dueAmount) {
        this.paymentMethod = method == null ? OrderPaymentMethod.ONLINE : method;
        this.paymentDueAmount = dueAmount;
        if (this.paymentMethod == OrderPaymentMethod.CASH_ON_DELIVERY
                || this.paymentMethod == OrderPaymentMethod.PAY_AFTER_DELIVERY_ONLINE) {
            this.paymentStatus = OrderPaymentStatus.DUE;
            this.paymentProvider = this.paymentMethod == OrderPaymentMethod.CASH_ON_DELIVERY ? "COD" : null;
            this.paymentDueAt = null;
        }
    }

    public void markOfflinePaymentPaid(String provider, String reference, String collectedBy) {
        this.paymentProvider = provider;
        this.paymentProviderReference = reference;
        this.paymentCollectedBy = collectedBy;
        this.paymentCollectionReference = reference;
        this.paymentStatus = OrderPaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void prepareOnlinePaymentAfterDelivery() {
        this.paymentMethod = OrderPaymentMethod.PAY_AFTER_DELIVERY_ONLINE;
    }

    public void markPaymentDueAt(LocalDateTime dueAt) {
        this.paymentDueAt = dueAt;
    }

    public void requestCancellation(String reason, String requestedBy) {
        this.cancellationStatus = OrderCancellationStatus.REQUESTED;
        this.cancellationReason = reason;
        this.cancellationRequestedBy = requestedBy;
        this.cancellationRequestedAt = LocalDateTime.now();
    }

    public void decideCancellation(boolean approved, String note) {
        this.cancellationStatus = approved ? OrderCancellationStatus.APPROVED : OrderCancellationStatus.REJECTED;
        this.cancellationDecisionNote = note;
    }

    public void markPaymentPaid(String providerReference) {
        this.paymentProviderReference = providerReference;
        this.paymentStatus = OrderPaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markPaymentFailed(String providerReference) {
        this.paymentProviderReference = providerReference;
        this.paymentStatus = OrderPaymentStatus.FAILED;
    }

    public List<OrderRefund> getRefunds() {
        return refunds;
    }

    public void addRefund(OrderRefund refund) {
        refunds.add(refund);
    }
}

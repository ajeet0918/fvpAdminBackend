package com.agriplatform.backend.customer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Customer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Customer.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 180)
    private String companyName;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(nullable = false, length = 400)
    private String deliveryAddress;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 120)
    private String state;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerAuthProvider authProvider;

    @Column(length = 255)
    private String googleSubject;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(length = 40)
    private String preferredPaymentMethod;

    @Column(length = 120)
    private String preferredPaymentHandle;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean deferredPaymentEligible;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Customer() {
    }

    public Customer(
            String fullName,
            String companyName,
            String email,
            String phone,
            String deliveryAddress,
            String city,
            String state,
            String postalCode
    ) {
        this.fullName = fullName;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.authProvider = CustomerAuthProvider.LOCAL;
        this.emailVerified = false;
        this.active = true;
        this.deferredPaymentEligible = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public CustomerAuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getGoogleSubject() {
        return googleSubject;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public String getPreferredPaymentHandle() {
        return preferredPaymentHandle;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDeferredPaymentEligible() {
        return deferredPaymentEligible;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateProfile(
            String fullName,
            String companyName,
            String email,
            String phone,
            String deliveryAddress,
            String city,
            String state,
            String postalCode
    ) {
        this.fullName = fullName;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.updatedAt = LocalDateTime.now();
    }

    public void setLocalAuth(String passwordHash) {
        this.passwordHash = passwordHash;
        this.authProvider = CustomerAuthProvider.LOCAL;
        this.updatedAt = LocalDateTime.now();
    }

    public void setGoogleAuth(String googleSubject) {
        this.googleSubject = googleSubject;
        this.authProvider = CustomerAuthProvider.GOOGLE;
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPreferredPayment(String method, String handle) {
        this.preferredPaymentMethod = method;
        this.preferredPaymentHandle = handle;
        this.updatedAt = LocalDateTime.now();
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDeferredPaymentEligible(boolean eligible) {
        this.deferredPaymentEligible = eligible;
        this.updatedAt = LocalDateTime.now();
    }
}

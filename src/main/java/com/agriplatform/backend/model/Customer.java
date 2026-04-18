package com.agriplatform.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 180)
    private String companyName;

    @Column(nullable = false, length = 255)
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

    @Column(nullable = false)
    private boolean active;

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
        this.active = true;
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

    public boolean isActive() {
        return active;
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

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }
}

package com.agriplatform.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(length = 255)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(length = 1200)
    private String notes;

    @Column(length = 120)
    private String assignedTo;

    private Long inquiryId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Lead() {
    }

    public Lead(
            String fullName,
            String email,
            String phone,
            String companyName,
            String source,
            String notes,
            String assignedTo,
            Long inquiryId
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.companyName = companyName;
        this.status = LeadStatus.NEW;
        this.source = source;
        this.notes = notes;
        this.assignedTo = assignedTo;
        this.inquiryId = inquiryId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public String getSource() {
        return source;
    }

    public String getNotes() {
        return notes;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public Long getInquiryId() {
        return inquiryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String fullName,
            String email,
            String phone,
            String companyName,
            LeadStatus status,
            String notes,
            String assignedTo,
            String source,
            Long inquiryId
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.companyName = companyName;
        this.status = status;
        this.notes = notes;
        this.assignedTo = assignedTo;
        this.source = source;
        this.inquiryId = inquiryId;
        this.updatedAt = LocalDateTime.now();
    }
}

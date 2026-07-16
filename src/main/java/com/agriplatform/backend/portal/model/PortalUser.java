package com.agriplatform.backend.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "portal_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_portal_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_portal_user_source_inquiry", columnNames = "source_inquiry_id")
        }
)
public class PortalUser {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalUser.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String username;

    @Column(length = 160)
    private String email;

    @Column(length = 40)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PortalUserType userType;

    @Column(name = "source_inquiry_id")
    private Long sourceInquiryId;

    @Column(length = 255)
    private String passwordHash;

    @Column(name = "reset_password", nullable = false)
    private boolean resetPassword = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PortalUserStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime activatedAt;

    private LocalDateTime lastLoginAt;

    protected PortalUser() {
    }

    public PortalUser(
            String username,
            String email,
            String phone,
            PortalUserType userType,
            Long sourceInquiryId
    ) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.sourceInquiryId = sourceInquiryId;
        this.status = PortalUserStatus.INVITED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public PortalUserType getUserType() {
        return userType;
    }

    public Long getSourceInquiryId() {
        return sourceInquiryId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isResetPassword() {
        return resetPassword;
    }

    public PortalUserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void refreshContact(String email, String phone, PortalUserType userType, Long sourceInquiryId) {
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.sourceInquiryId = sourceInquiryId;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate(String passwordHash) {
        this.passwordHash = passwordHash;
        this.status = PortalUserStatus.ACTIVE;
        this.resetPassword = false;
        this.activatedAt = LocalDateTime.now();
        this.updatedAt = this.activatedAt;
    }

    public void setTemporaryPassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.resetPassword = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.resetPassword = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = this.lastLoginAt;
    }
}

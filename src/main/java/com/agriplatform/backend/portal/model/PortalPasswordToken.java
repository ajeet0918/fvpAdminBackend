package com.agriplatform.backend.portal.model;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "portal_password_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_portal_password_token_hash", columnNames = "token_hash")
)
public class PortalPasswordToken {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalPasswordToken.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portal_user_id", nullable = false)
    private PortalUser portalUser;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PortalPasswordTokenPurpose purpose;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PortalPasswordToken() {
    }

    public PortalPasswordToken(
            PortalUser portalUser,
            String tokenHash,
            PortalPasswordTokenPurpose purpose,
            LocalDateTime expiresAt
    ) {
        this.portalUser = portalUser;
        this.tokenHash = tokenHash;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public PortalUser getPortalUser() {
        return portalUser;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public PortalPasswordTokenPurpose getPurpose() {
        return purpose;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }
}

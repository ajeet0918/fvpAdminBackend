package com.agriplatform.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "portal_otp_challenge")
public class PortalOtpChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String identifier;

    @Column(nullable = false, length = 255)
    private String normalizedIdentifier;

    @Column(nullable = false, length = 255)
    private String otpHash;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime consumedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PortalOtpChallenge() {
    }

    public PortalOtpChallenge(String identifier, String normalizedIdentifier, String otpHash, LocalDateTime expiresAt) {
        this.identifier = identifier;
        this.normalizedIdentifier = normalizedIdentifier;
        this.otpHash = otpHash;
        this.attemptCount = 0;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getNormalizedIdentifier() {
        return normalizedIdentifier;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void markAttempt() {
        this.attemptCount = this.attemptCount + 1;
    }

    public void consume() {
        this.consumedAt = LocalDateTime.now();
    }
}

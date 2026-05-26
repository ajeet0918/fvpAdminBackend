package com.agriplatform.backend.settings.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "smtpconfig")
public class SmtpConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SmtpConfig.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 255)
    private String host;

    private Integer port;

    @Column(length = 255)
    private String username;

    @Column(length = 1000)
    private String password;

    @Column(length = 255)
    private String fromEmail;

    @Column(length = 160)
    private String fromName;

    @Column(nullable = false)
    private boolean authEnabled;

    @Column(nullable = false)
    private boolean startTlsEnabled;

    @Column(length = 500)
    private String frontendBaseUrl;

    @Column(length = 120)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected SmtpConfig() {
    }

    public SmtpConfig(
            boolean active,
            String host,
            Integer port,
            String username,
            String password,
            String fromEmail,
            String fromName,
            boolean authEnabled,
            boolean startTlsEnabled,
            String frontendBaseUrl,
            String updatedBy
    ) {
        this.active = active;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.authEnabled = authEnabled;
        this.startTlsEnabled = startTlsEnabled;
        this.frontendBaseUrl = frontendBaseUrl;
        this.updatedBy = updatedBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public boolean isStartTlsEnabled() {
        return startTlsEnabled;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            boolean active,
            String host,
            Integer port,
            String username,
            String password,
            String fromEmail,
            String fromName,
            boolean authEnabled,
            boolean startTlsEnabled,
            String frontendBaseUrl,
            String updatedBy
    ) {
        this.active = active;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.authEnabled = authEnabled;
        this.startTlsEnabled = startTlsEnabled;
        this.frontendBaseUrl = frontendBaseUrl;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}

package com.agriplatform.backend.settings.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class AppSetting {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppSetting.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String settingKey;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppSettingValueType valueType;

    @Column(nullable = false)
    private boolean secret;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 500)
    private String description;

    @Column(length = 120)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected AppSetting() {
    }

    public AppSetting(
            String settingKey,
            String category,
            String settingValue,
            AppSettingValueType valueType,
            boolean secret,
            boolean active,
            String description,
            String updatedBy
    ) {
        this.settingKey = settingKey;
        this.category = category;
        this.settingValue = settingValue;
        this.valueType = valueType;
        this.secret = secret;
        this.active = active;
        this.description = description;
        this.updatedBy = updatedBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public String getCategory() {
        return category;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public AppSettingValueType getValueType() {
        return valueType;
    }

    public boolean isSecret() {
        return secret;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
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
            String category,
            String settingValue,
            AppSettingValueType valueType,
            boolean secret,
            boolean active,
            String description,
            String updatedBy
    ) {
        this.category = category;
        this.settingValue = settingValue;
        this.valueType = valueType;
        this.secret = secret;
        this.active = active;
        this.description = description;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}

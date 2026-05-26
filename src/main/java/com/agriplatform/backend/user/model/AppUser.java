package com.agriplatform.backend.user.model;

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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class AppUser {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppUser.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(length = 160)
    private String fullName;

    @Column(length = 80)
    private String firstName;

    @Column(length = 80)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private AppRole role;

    public AppUser() {
    }

    public AppUser(
            String username,
            String firstName,
            String lastName,
            String email,
            String phone,
            String passwordHash,
            AppRole role
    ) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = buildFullName(firstName, lastName);
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.role = role;
    }

    public AppUser(String username, String passwordHash, AppRole role) {
        this(username, username, "", null, null, passwordHash, role);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public AppRole getRole() {
        return role;
    }

    public void assignRole(AppRole role) {
        this.role = role;
    }

    public void activate() {
        this.active = true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void updateProfile(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = buildFullName(firstName, lastName);
        this.email = email;
        this.phone = phone;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    private String buildFullName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String combined = (first + " " + last).trim();
        return combined.isBlank() ? null : combined;
    }
}

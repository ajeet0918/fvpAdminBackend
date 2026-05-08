package com.agriplatform.backend.lead.service;

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

import java.util.Comparator;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadService {

    private final LeadRepository leadRepository;

    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Transactional
    public LeadResponse createPublicLead(LeadRequest request) {
        Lead lead = new Lead(
                request.fullName().trim(),
                request.email().trim().toLowerCase(),
                request.phone().trim(),
                normalizeNullable(request.companyName()),
                "WEBSITE_CONTACT",
                normalizeNullable(request.notes()),
                null,
                null
        );
        return mapLead(leadRepository.save(lead));
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getLeads(String search, String status, String source, String assignedTo) {
        String normalizedSearch = normalizeSearch(search);
        LeadStatus statusFilter = parseStatusNullable(status);
        String sourceFilter = normalizeUpperNullable(source);
        String assignedToFilter = resolveAssignedToFilter(assignedTo);

        return leadRepository.findAll().stream()
                .filter(lead -> matchesSearch(lead, normalizedSearch))
                .filter(lead -> statusFilter == null || lead.getStatus() == statusFilter)
                .filter(lead -> sourceFilter == null || sourceFilter.equalsIgnoreCase(lead.getSource()))
                .filter(lead -> assignedToFilter == null || containsIgnoreCase(lead.getAssignedTo(), assignedToFilter))
                .sorted(Comparator.comparing(Lead::getCreatedAt).reversed())
                .map(this::mapLead)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeadResponse getLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));
        return mapLead(lead);
    }

    @Transactional
    public LeadResponse createLead(CreateLeadRequest request) {
        Lead lead = new Lead(
                request.fullName().trim(),
                request.email().trim().toLowerCase(),
                request.phone().trim(),
                normalizeNullable(request.companyName()),
                normalizeSource(request.source()),
                normalizeNullable(request.notes()),
                normalizeAssignedToForActor(request.assignedTo()),
                request.inquiryId()
        );
        return mapLead(leadRepository.save(lead));
    }

    @Transactional
    public LeadResponse updateLead(Long id, UpdateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

        lead.update(
                request.fullName().trim(),
                request.email().trim().toLowerCase(),
                request.phone().trim(),
                normalizeNullable(request.companyName()),
                parseStatus(request.status()),
                normalizeNullable(request.notes()),
                normalizeAssignedToForActor(request.assignedTo()),
                normalizeSource(request.source()),
                request.inquiryId()
        );
        return mapLead(leadRepository.save(lead));
    }

    @Transactional
    public void deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new IllegalArgumentException("Lead not found");
        }
        leadRepository.deleteById(id);
    }

    @Transactional
    public LeadResponse createLeadFromInquiry(
            String fullName,
            String email,
            String phone,
            String companyName,
            String notes,
            String assignedTo,
            Long inquiryId
    ) {
        Lead lead = new Lead(
                fullName.trim(),
                email.trim().toLowerCase(),
                phone.trim(),
                normalizeNullable(companyName),
                "INQUIRY_CONVERSION",
                normalizeNullable(notes),
                normalizeAssignedToForActor(assignedTo),
                inquiryId
        );
        return mapLead(leadRepository.save(lead));
    }

    private LeadResponse mapLead(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getFullName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getCompanyName(),
                lead.getStatus().name(),
                lead.getSource(),
                lead.getNotes(),
                lead.getAssignedTo(),
                lead.getInquiryId(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }

    private LeadStatus parseStatus(String value) {
        try {
            return LeadStatus.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid lead status");
        }
    }

    private LeadStatus parseStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseStatus(value);
    }

    private String normalizeSource(String value) {
        if (value == null || value.isBlank()) {
            return "ADMIN_PANEL";
        }
        return value.trim().toUpperCase();
    }

    private String normalizeUpperNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private boolean matchesSearch(Lead lead, String search) {
        if (search == null) {
            return true;
        }
        return containsIgnoreCase(lead.getFullName(), search)
                || containsIgnoreCase(lead.getEmail(), search)
                || containsIgnoreCase(lead.getPhone(), search)
                || containsIgnoreCase(lead.getCompanyName(), search)
                || containsIgnoreCase(lead.getSource(), search);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private String resolveAssignedToFilter(String assignedTo) {
        if ("SALES".equals(getCurrentRole())) {
            return normalizeSearch(getCurrentUsername());
        }
        return normalizeSearch(assignedTo);
    }

    private String normalizeAssignedToForActor(String assignedTo) {
        if ("SALES".equals(getCurrentRole())) {
            return normalizeNullable(getCurrentUsername());
        }
        return normalizeNullable(assignedTo);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("");
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "";
    }
}

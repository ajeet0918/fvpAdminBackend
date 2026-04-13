package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.CreateLeadRequest;
import com.agriplatform.backend.dto.LeadRequest;
import com.agriplatform.backend.dto.LeadResponse;
import com.agriplatform.backend.dto.UpdateLeadRequest;
import com.agriplatform.backend.model.Lead;
import com.agriplatform.backend.model.LeadStatus;
import com.agriplatform.backend.repository.LeadRepository;
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

package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.CreateLeadRequest;
import com.agriplatform.backend.dto.LeadResponse;
import com.agriplatform.backend.dto.UpdateLeadRequest;
import com.agriplatform.backend.service.LeadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/leads")
public class AdminLeadController {

    private final LeadService leadService;

    public AdminLeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public List<LeadResponse> getLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String assignedTo
    ) {
        return leadService.getLeads(search, status, source, assignedTo);
    }

    @GetMapping("/{id}")
    public LeadResponse getLead(@PathVariable Long id) {
        return leadService.getLead(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse createLead(@Valid @RequestBody CreateLeadRequest request) {
        return leadService.createLead(request);
    }

    @PutMapping("/{id}")
    public LeadResponse updateLead(@PathVariable Long id, @Valid @RequestBody UpdateLeadRequest request) {
        return leadService.updateLead(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
    }
}

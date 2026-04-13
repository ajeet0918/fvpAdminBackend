package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.ConvertInquiryToLeadRequest;
import com.agriplatform.backend.dto.InquiryResponse;
import com.agriplatform.backend.dto.UpdateInquiryRequest;
import com.agriplatform.backend.service.InquiryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    public AdminInquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping
    public List<InquiryResponse> getInquiries(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String inquiryType
    ) {
        return inquiryService.getInquiries(search, status, source, assignedTo, inquiryType);
    }

    @GetMapping("/{id}")
    public InquiryResponse getInquiry(@PathVariable Long id) {
        return inquiryService.getInquiry(id);
    }

    @PutMapping("/{id}")
    public InquiryResponse updateInquiry(@PathVariable Long id, @Valid @RequestBody UpdateInquiryRequest request) {
        return inquiryService.updateInquiry(id, request);
    }

    @PostMapping("/{id}/convert-to-lead")
    public InquiryResponse convertToLead(
            @PathVariable Long id,
            @Valid @RequestBody ConvertInquiryToLeadRequest request
    ) {
        return inquiryService.convertToLead(id, request);
    }
}

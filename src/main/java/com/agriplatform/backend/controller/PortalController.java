package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.PortalSummaryResponse;
import com.agriplatform.backend.service.PortalSummaryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final PortalSummaryService portalSummaryService;

    public PortalController(PortalSummaryService portalSummaryService) {
        this.portalSummaryService = portalSummaryService;
    }

    @GetMapping("/summary")
    public PortalSummaryResponse getSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication != null ? authentication.getName() : "";
        return portalSummaryService.getSummary(identifier);
    }

    @GetMapping("/receipts/{receiptNumber}/download")
    public ResponseEntity<String> downloadReceipt(@PathVariable String receiptNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication != null ? authentication.getName() : "";
        String content = portalSummaryService.downloadReceipt(identifier, receiptNumber);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + receiptNumber + ".txt\"")
                .body(content);
    }
}

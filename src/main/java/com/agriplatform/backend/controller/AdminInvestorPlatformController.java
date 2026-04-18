package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.CreateInvestmentRequest;
import com.agriplatform.backend.dto.CreateInvestorAccountRequest;
import com.agriplatform.backend.dto.CreateInvestorPayoutRequest;
import com.agriplatform.backend.dto.GenerateMonthlyReturnsRequest;
import com.agriplatform.backend.dto.InvestmentResponse;
import com.agriplatform.backend.dto.InvestorAccountResponse;
import com.agriplatform.backend.dto.InvestorMonthlyReturnResponse;
import com.agriplatform.backend.dto.InvestorOverviewResponse;
import com.agriplatform.backend.dto.InvestorPayoutResponse;
import com.agriplatform.backend.dto.InvestorProfileResponse;
import com.agriplatform.backend.dto.InvestorProfileUpsertRequest;
import com.agriplatform.backend.dto.InvestorReceiptResponse;
import com.agriplatform.backend.dto.MarkInvestorPayoutPaidRequest;
import com.agriplatform.backend.dto.PayoutActionRequest;
import com.agriplatform.backend.dto.ReturnActionRequest;
import com.agriplatform.backend.dto.UpdateInvestmentRequest;
import com.agriplatform.backend.dto.UpdateInvestorAccountRequest;
import com.agriplatform.backend.dto.UpdateInvestorMonthlyReturnRequest;
import com.agriplatform.backend.service.InvestorPlatformService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/investor-platform")
public class AdminInvestorPlatformController {

    private final InvestorPlatformService investorPlatformService;

    public AdminInvestorPlatformController(InvestorPlatformService investorPlatformService) {
        this.investorPlatformService = investorPlatformService;
    }

    @GetMapping("/investors")
    public List<InvestorAccountResponse> getInvestors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String verificationStatus
    ) {
        return investorPlatformService.getInvestors(search, status, verificationStatus);
    }

    @GetMapping("/overview")
    public InvestorOverviewResponse getOverview(
            @RequestParam(required = false) String investorSearch,
            @RequestParam(required = false) String investorStatus,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String investmentSearch,
            @RequestParam(required = false) String investmentStatus
    ) {
        return investorPlatformService.getOverview(
                investorSearch,
                investorStatus,
                verificationStatus,
                investmentSearch,
                investmentStatus
        );
    }

    @GetMapping("/investors/{id}")
    public InvestorAccountResponse getInvestor(@PathVariable Long id) {
        return investorPlatformService.getInvestor(id);
    }

    @PostMapping("/investors")
    public InvestorAccountResponse createInvestor(@Valid @RequestBody CreateInvestorAccountRequest request) {
        return investorPlatformService.createInvestor(request);
    }

    @PutMapping("/investors/{id}")
    public InvestorAccountResponse updateInvestor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvestorAccountRequest request
    ) {
        return investorPlatformService.updateInvestor(id, request);
    }

    @PostMapping("/profiles")
    public InvestorProfileResponse createInvestorProfile(@Valid @RequestBody InvestorProfileUpsertRequest request) {
        return investorPlatformService.createInvestorProfile(request);
    }

    @PutMapping("/profiles/{id}")
    public InvestorProfileResponse updateInvestorProfile(
            @PathVariable Long id,
            @Valid @RequestBody InvestorProfileUpsertRequest request
    ) {
        return investorPlatformService.updateInvestorProfile(id, request);
    }

    @GetMapping("/investments")
    public List<InvestmentResponse> getInvestments(
            @RequestParam(required = false) Long investorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        return investorPlatformService.getInvestments(investorId, status, search);
    }

    @GetMapping("/investments/{id}")
    public InvestmentResponse getInvestment(@PathVariable Long id) {
        return investorPlatformService.getInvestment(id);
    }

    @PostMapping("/investments")
    public InvestmentResponse createInvestment(@Valid @RequestBody CreateInvestmentRequest request) {
        return investorPlatformService.createInvestment(request);
    }

    @PutMapping("/investments/{id}")
    public InvestmentResponse updateInvestment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvestmentRequest request
    ) {
        return investorPlatformService.updateInvestment(id, request);
    }

    @PostMapping("/returns/generate")
    public List<InvestorMonthlyReturnResponse> generateMonthlyReturns(@Valid @RequestBody GenerateMonthlyReturnsRequest request) {
        return investorPlatformService.generateMonthlyReturns(request);
    }

    @GetMapping("/returns")
    public List<InvestorMonthlyReturnResponse> getMonthlyReturns(
            @RequestParam(required = false) Long investorId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String status
    ) {
        return investorPlatformService.getMonthlyReturns(investorId, year, month, status);
    }

    @PutMapping("/returns/{id}")
    public InvestorMonthlyReturnResponse updateMonthlyReturn(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvestorMonthlyReturnRequest request
    ) {
        return investorPlatformService.updateMonthlyReturn(id, request);
    }

    @PostMapping("/returns/{id}/submit")
    public InvestorMonthlyReturnResponse submitMonthlyReturn(
            @PathVariable Long id,
            @Valid @RequestBody ReturnActionRequest request
    ) {
        return investorPlatformService.submitMonthlyReturn(id, request);
    }

    @PostMapping("/returns/{id}/approve")
    public InvestorMonthlyReturnResponse approveMonthlyReturn(
            @PathVariable Long id,
            @Valid @RequestBody ReturnActionRequest request
    ) {
        return investorPlatformService.approveMonthlyReturn(id, request);
    }

    @PostMapping("/returns/{id}/reject")
    public InvestorMonthlyReturnResponse rejectMonthlyReturn(
            @PathVariable Long id,
            @Valid @RequestBody ReturnActionRequest request
    ) {
        return investorPlatformService.rejectMonthlyReturn(id, request);
    }

    @PostMapping("/returns/{id}/hold")
    public InvestorMonthlyReturnResponse holdMonthlyReturn(
            @PathVariable Long id,
            @Valid @RequestBody ReturnActionRequest request
    ) {
        return investorPlatformService.holdMonthlyReturn(id, request);
    }

    @GetMapping("/payouts")
    public List<InvestorPayoutResponse> getPayouts(
            @RequestParam(required = false) Long investorId,
            @RequestParam(required = false) String status
    ) {
        return investorPlatformService.getPayouts(investorId, status);
    }

    @PostMapping("/payouts/request")
    public InvestorPayoutResponse createPayoutRequest(@Valid @RequestBody CreateInvestorPayoutRequest request) {
        return investorPlatformService.createPayoutRequest(request);
    }

    @PostMapping("/payouts/{id}/approve")
    public InvestorPayoutResponse approvePayout(
            @PathVariable Long id,
            @Valid @RequestBody PayoutActionRequest request
    ) {
        return investorPlatformService.approvePayout(id, request);
    }

    @PostMapping("/payouts/{id}/reject")
    public InvestorPayoutResponse rejectPayout(
            @PathVariable Long id,
            @Valid @RequestBody PayoutActionRequest request
    ) {
        return investorPlatformService.rejectPayout(id, request);
    }

    @PostMapping("/payouts/{id}/mark-paid")
    public InvestorPayoutResponse markPayoutPaid(
            @PathVariable Long id,
            @Valid @RequestBody MarkInvestorPayoutPaidRequest request
    ) {
        return investorPlatformService.markPayoutPaid(id, request);
    }

    @PostMapping("/payouts/{id}/generate-receipt")
    public InvestorReceiptResponse generateReceipt(@PathVariable Long id) {
        return investorPlatformService.generateReceipt(id);
    }

    @GetMapping("/receipts")
    public List<InvestorReceiptResponse> getReceipts(@RequestParam(required = false) Long investorId) {
        return investorPlatformService.getReceipts(investorId);
    }

    @GetMapping("/receipts/{id}")
    public InvestorReceiptResponse getReceipt(@PathVariable Long id) {
        return investorPlatformService.getReceipt(id);
    }

    @GetMapping("/receipts/number/{receiptNumber}/download")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String receiptNumber) {
        byte[] content = investorPlatformService.buildReceiptPdf(receiptNumber);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + receiptNumber + ".pdf\"")
                .body(content);
    }
}

package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.CollectionHubInquiryRequest;
import com.agriplatform.backend.dto.FarmerInquiryRequest;
import com.agriplatform.backend.dto.InquiryRequest;
import com.agriplatform.backend.dto.InquiryResponse;
import com.agriplatform.backend.dto.InvestorInquiryRequest;
import com.agriplatform.backend.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryResponse createInquiry(@Valid @RequestBody InquiryRequest request) {
        return inquiryService.createInquiry(request);
    }

    @PostMapping(value = "/investor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryResponse createInvestorInquiry(
            @Valid @RequestPart("payload") InvestorInquiryRequest request,
            @RequestPart(value = "idProof", required = false) MultipartFile idProof,
            @RequestPart(value = "paymentScreenshot", required = false) MultipartFile paymentScreenshot
    ) {
        return inquiryService.createInvestorInquiry(request, idProof, paymentScreenshot);
    }

    @PostMapping(value = "/farmer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryResponse createFarmerInquiry(
            @Valid @RequestPart("payload") FarmerInquiryRequest request,
            @RequestPart("aadhaarDocument") MultipartFile aadhaarDocument,
            @RequestPart(value = "landProofDocument", required = false) MultipartFile landProofDocument,
            @RequestPart(value = "bankPassbookDocument", required = false) MultipartFile bankPassbookDocument
    ) {
        return inquiryService.createFarmerInquiry(request, aadhaarDocument, landProofDocument, bankPassbookDocument);
    }

    @PostMapping(value = "/collection-hub", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryResponse createCollectionHubInquiry(
            @Valid @RequestPart("payload") CollectionHubInquiryRequest request,
            @RequestPart(value = "hubDocument", required = false) MultipartFile hubDocument
    ) {
        return inquiryService.createCollectionHubInquiry(request, hubDocument);
    }
}

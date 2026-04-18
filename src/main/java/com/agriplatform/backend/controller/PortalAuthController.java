package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.PortalOtpRequest;
import com.agriplatform.backend.dto.PortalOtpRequestResponse;
import com.agriplatform.backend.dto.PortalOtpVerifyRequest;
import com.agriplatform.backend.dto.PortalOtpVerifyResponse;
import com.agriplatform.backend.service.PortalAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {

    private final PortalAuthService portalAuthService;

    public PortalAuthController(PortalAuthService portalAuthService) {
        this.portalAuthService = portalAuthService;
    }

    @PostMapping("/request-otp")
    public PortalOtpRequestResponse requestOtp(@Valid @RequestBody PortalOtpRequest request) {
        return portalAuthService.requestOtp(request.identifier());
    }

    @PostMapping("/verify-otp")
    public PortalOtpVerifyResponse verifyOtp(@Valid @RequestBody PortalOtpVerifyRequest request) {
        return portalAuthService.verifyOtp(request.identifier(), request.otp());
    }
}

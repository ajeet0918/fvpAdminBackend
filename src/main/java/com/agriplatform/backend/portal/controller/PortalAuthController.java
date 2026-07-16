package com.agriplatform.backend.portal.controller;

import com.agriplatform.backend.portal.dto.PortalActivateRequest;
import com.agriplatform.backend.portal.dto.PortalAuthResponse;
import com.agriplatform.backend.portal.dto.PortalLoginRequest;
import com.agriplatform.backend.portal.dto.PortalMessageResponse;
import com.agriplatform.backend.portal.dto.PortalPasswordChangeRequest;
import com.agriplatform.backend.portal.dto.PortalPasswordResetConfirmRequest;
import com.agriplatform.backend.portal.dto.PortalPasswordResetRequest;
import com.agriplatform.backend.portal.service.PortalAuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalAuthController.class);

    private final PortalAuthService portalAuthService;

    public PortalAuthController(PortalAuthService portalAuthService) {
        this.portalAuthService = portalAuthService;
    }

    @PostMapping("/login")
    public PortalAuthResponse login(@Valid @RequestBody PortalLoginRequest request) {
        return portalAuthService.login(request.username(), request.password());
    }

    @PostMapping("/activate")
    public PortalMessageResponse activate(@Valid @RequestBody PortalActivateRequest request) {
        return portalAuthService.activate(request.token(), request.password());
    }

    @PostMapping("/request-password-reset")
    public PortalMessageResponse requestPasswordReset(@Valid @RequestBody PortalPasswordResetRequest request) {
        return portalAuthService.requestPasswordReset(request.identifier());
    }

    @PostMapping("/reset-password")
    public PortalMessageResponse resetPassword(@Valid @RequestBody PortalPasswordResetConfirmRequest request) {
        return portalAuthService.resetPassword(request.token(), request.password());
    }

    @PostMapping("/change-password")
    public PortalMessageResponse changePassword(Authentication authentication, @Valid @RequestBody PortalPasswordChangeRequest request) {
        return portalAuthService.changeAuthenticatedPassword(authentication.getName(), request.password());
    }
}

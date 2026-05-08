package com.agriplatform.backend.customer.controller;

import com.agriplatform.backend.customer.dto.CustomerAuthResponse;
import com.agriplatform.backend.customer.dto.CustomerGoogleAuthRequest;
import com.agriplatform.backend.customer.dto.CustomerLoginRequest;
import com.agriplatform.backend.customer.dto.CustomerSignupRequest;
import com.agriplatform.backend.customer.service.CustomerAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/auth")
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    public CustomerAuthController(CustomerAuthService customerAuthService) {
        this.customerAuthService = customerAuthService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerAuthResponse signup(@Valid @RequestBody CustomerSignupRequest request) {
        return customerAuthService.signup(request);
    }

    @PostMapping("/login")
    public CustomerAuthResponse login(@Valid @RequestBody CustomerLoginRequest request) {
        return customerAuthService.login(request);
    }

    @PostMapping("/google")
    public CustomerAuthResponse google(@Valid @RequestBody CustomerGoogleAuthRequest request) {
        return customerAuthService.googleAuth(request);
    }
}

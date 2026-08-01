package com.agriplatform.backend.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
class SecurityProbeController {
    @GetMapping("/api/products/security-probe")
    String publicProducts() {
        return "public-products";
    }

    @GetMapping("/api/admin/products/security-probe")
    String adminProducts() {
        return "admin-products";
    }

    @GetMapping("/api/customer/me/security-probe")
    String customerAccount() {
        return "customer-account";
    }

    @PostMapping("/api/orders/1/refunds")
    String createRefund() {
        return "refund-created";
    }
}

package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.AdminCustomerResponse;
import com.agriplatform.backend.dto.UpdateCustomerRequest;
import com.agriplatform.backend.service.AdminCustomerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    public AdminCustomerController(AdminCustomerService adminCustomerService) {
        this.adminCustomerService = adminCustomerService;
    }

    @GetMapping
    public List<AdminCustomerResponse> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        return adminCustomerService.getCustomers(search, status);
    }

    @GetMapping("/{id}")
    public AdminCustomerResponse getCustomer(@PathVariable Long id) {
        return adminCustomerService.getCustomer(id);
    }

    @PutMapping("/{id}")
    public AdminCustomerResponse updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return adminCustomerService.updateCustomer(id, request);
    }
}

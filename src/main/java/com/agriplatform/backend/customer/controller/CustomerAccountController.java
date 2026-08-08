package com.agriplatform.backend.customer.controller;

import com.agriplatform.backend.customer.dto.CustomerAddressRequest;
import com.agriplatform.backend.customer.dto.CustomerAddressResponse;
import com.agriplatform.backend.customer.dto.CustomerProfileResponse;
import com.agriplatform.backend.customer.dto.UpdateCustomerProfileRequest;
import com.agriplatform.backend.customer.dto.UpdatePaymentPreferenceRequest;
import com.agriplatform.backend.customer.service.CustomerAccountService;
import com.agriplatform.backend.customer.service.CustomerAuthService;
import com.agriplatform.backend.customer.service.CustomerOrderService;
import com.agriplatform.backend.order.dto.CreateOrderPaymentSessionRequest;
import com.agriplatform.backend.order.dto.CreateCustomerOrderRequest;
import com.agriplatform.backend.order.dto.OrderPaymentSessionResponse;
import com.agriplatform.backend.order.dto.OrderResponse;
import com.agriplatform.backend.order.dto.CustomerCancellationRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/me")
public class CustomerAccountController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerAccountController.class);

    private final CustomerAuthService customerAuthService;
    private final CustomerAccountService customerAccountService;
    private final CustomerOrderService customerOrderService;

    public CustomerAccountController(
            CustomerAuthService customerAuthService,
            CustomerAccountService customerAccountService,
            CustomerOrderService customerOrderService
    ) {
        this.customerAuthService = customerAuthService;
        this.customerAccountService = customerAccountService;
        this.customerOrderService = customerOrderService;
    }

    @GetMapping
    public CustomerProfileResponse me(Authentication authentication) {
        return customerAccountService.getProfile(resolveCustomerId(authentication));
    }

    @PutMapping("/profile")
    public CustomerProfileResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateCustomerProfileRequest request
    ) {
        return customerAccountService.updateProfile(resolveCustomerId(authentication), request);
    }

    @PutMapping("/payment-preference")
    public CustomerProfileResponse updatePaymentPreference(
            Authentication authentication,
            @Valid @RequestBody UpdatePaymentPreferenceRequest request
    ) {
        return customerAccountService.updatePaymentPreference(resolveCustomerId(authentication), request);
    }

    @GetMapping("/addresses")
    public List<CustomerAddressResponse> listAddresses(Authentication authentication) {
        return customerAccountService.getAddresses(resolveCustomerId(authentication));
    }

    @PostMapping("/addresses")
    public CustomerAddressResponse createAddress(
            Authentication authentication,
            @Valid @RequestBody CustomerAddressRequest request
    ) {
        return customerAccountService.createAddress(resolveCustomerId(authentication), request);
    }

    @PutMapping("/addresses/{addressId}")
    public CustomerAddressResponse updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody CustomerAddressRequest request
    ) {
        return customerAccountService.updateAddress(resolveCustomerId(authentication), addressId, request);
    }

    @DeleteMapping("/addresses/{addressId}")
    public void deleteAddress(Authentication authentication, @PathVariable Long addressId) {
        customerAccountService.deleteAddress(resolveCustomerId(authentication), addressId);
    }

    @GetMapping("/orders")
    public List<OrderResponse> orders(Authentication authentication) {
        return customerOrderService.getOrderHistory(resolveCustomerId(authentication));
    }

    @PostMapping("/orders")
    public OrderPaymentSessionResponse createDirectOrder(
            Authentication authentication,
            @Valid @RequestBody CreateCustomerOrderRequest request
    ) {
        return customerOrderService.createDirectOrder(resolveCustomerId(authentication), request);
    }

    @PostMapping("/orders/{orderId}/payment-session")
    public OrderPaymentSessionResponse retryPaymentSession(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateOrderPaymentSessionRequest request
    ) {
        return customerOrderService.retryPaymentSession(resolveCustomerId(authentication), orderId, request);
    }

    @PostMapping("/orders/{orderId}/cancellation-request")
    public OrderResponse requestCancellation(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody CustomerCancellationRequest request
    ) {
        return customerOrderService.requestCancellation(resolveCustomerId(authentication), orderId, request);
    }

    private Long resolveCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Customer session not found");
        }
        return customerAuthService.parseCustomerIdFromSubject(authentication.getName());
    }
}

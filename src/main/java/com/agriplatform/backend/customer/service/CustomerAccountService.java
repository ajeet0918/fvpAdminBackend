package com.agriplatform.backend.customer.service;

import com.agriplatform.backend.customer.dto.CustomerAddressRequest;
import com.agriplatform.backend.customer.dto.CustomerAddressResponse;
import com.agriplatform.backend.customer.dto.CustomerProfileResponse;
import com.agriplatform.backend.customer.dto.UpdateCustomerProfileRequest;
import com.agriplatform.backend.customer.dto.UpdatePaymentPreferenceRequest;
import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.customer.model.CustomerAddress;
import com.agriplatform.backend.customer.repository.CustomerAddressRepository;
import com.agriplatform.backend.customer.repository.CustomerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerAccountService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerAccountService.class);

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    public CustomerAccountService(
            CustomerRepository customerRepository,
            CustomerAddressRepository customerAddressRepository
    ) {
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Long customerId) {
        return mapProfile(getCustomer(customerId));
    }

    @Transactional
    public CustomerProfileResponse updateProfile(Long customerId, UpdateCustomerProfileRequest request) {
        Customer customer = getCustomer(customerId);
        customer.updateProfile(
                request.fullName().trim(),
                request.companyName() == null || request.companyName().isBlank() ? "Individual" : request.companyName().trim(),
                customer.getEmail(),
                request.phone().trim(),
                request.deliveryAddress().trim(),
                request.city().trim(),
                request.state().trim(),
                request.postalCode().trim()
        );
        return mapProfile(customerRepository.save(customer));
    }

    @Transactional
    public CustomerProfileResponse updatePaymentPreference(Long customerId, UpdatePaymentPreferenceRequest request) {
        Customer customer = getCustomer(customerId);
        customer.setPreferredPayment(
                request.preferredPaymentMethod() == null ? null : request.preferredPaymentMethod().trim(),
                request.preferredPaymentHandle() == null ? null : request.preferredPaymentHandle().trim()
        );
        return mapProfile(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> getAddresses(Long customerId) {
        return customerAddressRepository.findByCustomer_IdOrderByIsDefaultDescUpdatedAtDesc(customerId).stream()
                .map(this::mapAddress)
                .toList();
    }

    @Transactional
    public CustomerAddressResponse createAddress(Long customerId, CustomerAddressRequest request) {
        Customer customer = getCustomer(customerId);
        if (request.isDefault()) {
            clearDefaultAddress(customerId);
        }
        CustomerAddress address = new CustomerAddress(
                customer,
                request.label().trim(),
                request.recipientName().trim(),
                request.phone().trim(),
                request.line1().trim(),
                request.line2() == null ? null : request.line2().trim(),
                request.city().trim(),
                request.state().trim(),
                request.postalCode().trim(),
                request.country().trim(),
                request.isDefault()
        );
        CustomerAddress saved = customerAddressRepository.save(address);
        return mapAddress(saved);
    }

    @Transactional
    public CustomerAddressResponse updateAddress(Long customerId, Long addressId, CustomerAddressRequest request) {
        CustomerAddress address = customerAddressRepository.findByIdAndCustomer_Id(addressId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (request.isDefault()) {
            clearDefaultAddress(customerId);
        }
        address.update(
                request.label().trim(),
                request.recipientName().trim(),
                request.phone().trim(),
                request.line1().trim(),
                request.line2() == null ? null : request.line2().trim(),
                request.city().trim(),
                request.state().trim(),
                request.postalCode().trim(),
                request.country().trim(),
                request.isDefault()
        );
        return mapAddress(customerAddressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long customerId, Long addressId) {
        CustomerAddress address = customerAddressRepository.findByIdAndCustomer_Id(addressId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        customerAddressRepository.delete(address);
    }

    private Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    private void clearDefaultAddress(Long customerId) {
        List<CustomerAddress> addresses = customerAddressRepository.findByCustomer_IdOrderByIsDefaultDescUpdatedAtDesc(customerId);
        for (CustomerAddress item : addresses) {
            if (item.isDefault()) {
                item.setDefault(false);
            }
        }
        if (!addresses.isEmpty()) {
            customerAddressRepository.saveAll(addresses);
        }
    }

    private CustomerProfileResponse mapProfile(Customer customer) {
        return new CustomerProfileResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getCompanyName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDeliveryAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getPostalCode(),
                customer.getPreferredPaymentMethod(),
                customer.getPreferredPaymentHandle(),
                customer.isDeferredPaymentEligible()
        );
    }

    private CustomerAddressResponse mapAddress(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefault()
        );
    }
}

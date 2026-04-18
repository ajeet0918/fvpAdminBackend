package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.AdminCustomerResponse;
import com.agriplatform.backend.dto.UpdateCustomerRequest;
import com.agriplatform.backend.model.Customer;
import com.agriplatform.backend.model.PurchaseOrder;
import com.agriplatform.backend.repository.CustomerRepository;
import com.agriplatform.backend.repository.PurchaseOrderRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCustomerService {

    private final CustomerRepository customerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public AdminCustomerService(
            CustomerRepository customerRepository,
            PurchaseOrderRepository purchaseOrderRepository
    ) {
        this.customerRepository = customerRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCustomerResponse> getCustomers(String search, String status) {
        String searchFilter = normalizeSearch(search);
        Boolean activeFilter = parseActiveFilter(status);

        List<Customer> customers = customerRepository.findAll().stream()
                .filter(customer -> matchesSearch(customer, searchFilter))
                .filter(customer -> activeFilter == null || customer.isActive() == activeFilter)
                .sorted(Comparator.comparing(Customer::getUpdatedAt).reversed())
                .toList();

        List<PurchaseOrder> orders = purchaseOrderRepository.findAll().stream()
                .filter(order -> order.getCustomer() != null && order.getCustomer().getId() != null)
                .sorted(Comparator.comparing(PurchaseOrder::getCreatedAt).reversed())
                .toList();

        Map<Long, List<PurchaseOrder>> ordersByCustomerId = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(order -> order.getCustomer().getId()));

        return customers.stream()
                .map(customer -> mapCustomer(customer, ordersByCustomerId.get(customer.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminCustomerResponse getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        List<PurchaseOrder> orders = purchaseOrderRepository.findByCustomer_IdOrderByCreatedAtDesc(id);
        return mapCustomer(customer, orders);
    }

    @Transactional
    public AdminCustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = request.phone().trim();

        customer.updateProfile(
                request.fullName().trim(),
                request.companyName().trim(),
                normalizedEmail,
                normalizedPhone,
                request.deliveryAddress().trim(),
                request.city().trim(),
                request.state().trim(),
                request.postalCode().trim()
        );
        customer.setActive(request.active());

        Customer saved = customerRepository.save(customer);
        List<PurchaseOrder> orders = purchaseOrderRepository.findByCustomer_IdOrderByCreatedAtDesc(id);
        return mapCustomer(saved, orders);
    }

    private AdminCustomerResponse mapCustomer(Customer customer, List<PurchaseOrder> orders) {
        long totalOrders = orders == null ? 0 : orders.size();
        String lastOrderNumber = null;
        LocalDateTime lastOrderAt = null;
        if (orders != null && !orders.isEmpty()) {
            PurchaseOrder latestOrder = orders.get(0);
            lastOrderNumber = latestOrder.getOrderNumber();
            lastOrderAt = latestOrder.getCreatedAt();
        }

        return new AdminCustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getCompanyName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDeliveryAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getPostalCode(),
                customer.isActive(),
                customer.isActive() ? "ACTIVE" : "INACTIVE",
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                totalOrders,
                lastOrderNumber,
                lastOrderAt
        );
    }

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private Boolean parseActiveFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("ACTIVE".equals(normalized)) {
            return true;
        }
        if ("INACTIVE".equals(normalized)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid customer status filter");
    }

    private boolean matchesSearch(Customer customer, String search) {
        if (search == null) {
            return true;
        }
        return containsIgnoreCase(customer.getFullName(), search)
                || containsIgnoreCase(customer.getCompanyName(), search)
                || containsIgnoreCase(customer.getEmail(), search)
                || containsIgnoreCase(customer.getPhone(), search)
                || containsIgnoreCase(customer.getCity(), search)
                || containsIgnoreCase(customer.getState(), search);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }
}

package com.agriplatform.backend.order.service;

import com.agriplatform.backend.customer.model.Customer;
import com.agriplatform.backend.customer.model.CustomerAddress;
import com.agriplatform.backend.customer.repository.CustomerAddressRepository;
import com.agriplatform.backend.customer.repository.CustomerRepository;
import com.agriplatform.backend.order.dto.CreateCustomerOrderRequest;
import com.agriplatform.backend.order.dto.CreateOrderItemRequest;
import com.agriplatform.backend.order.dto.CreateOrderRequest;
import com.agriplatform.backend.order.dto.OrderItemResponse;
import com.agriplatform.backend.order.dto.OrderResponse;
import com.agriplatform.backend.order.dto.OrderStatusHistoryResponse;
import com.agriplatform.backend.order.dto.QuoteOrderItemRequest;
import com.agriplatform.backend.order.dto.QuoteOrderRequest;
import com.agriplatform.backend.order.dto.UpdateOrderStatusRequest;
import com.agriplatform.backend.order.model.OrderItem;
import com.agriplatform.backend.order.model.OrderStatusHistory;
import com.agriplatform.backend.order.model.PurchaseOrder;
import com.agriplatform.backend.order.model.PurchaseOrderStatus;
import com.agriplatform.backend.order.repository.PurchaseOrderRepository;
import com.agriplatform.backend.product.model.Product;
import com.agriplatform.backend.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;

    public OrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            CustomerAddressRepository customerAddressRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = getOrCreateCustomer(request);

        PurchaseOrder purchaseOrder = new PurchaseOrder(
                generateOrderNumber(),
                request.fullName(),
                request.companyName(),
                request.email(),
                request.phone(),
                request.deliveryAddress(),
                request.city(),
                request.state(),
                request.postalCode(),
                request.customerNotes()
        );
        purchaseOrder.setCustomer(customer);

        request.items().stream()
                .map(this::mapOrderItem)
                .forEach(purchaseOrder::addItem);

        applyAutomaticPricing(purchaseOrder, "Product-based pricing applied automatically.");

        purchaseOrder.addStatusHistory(new OrderStatusHistory(
                PurchaseOrderStatus.PENDING_REVIEW,
                "Order request submitted by customer."
        ));

        return mapOrder(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public OrderResponse createOrderForCustomer(Long customerId, CreateCustomerOrderRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        CustomerAddress selectedAddress = resolveAddress(customer, request.addressId());
        String customerNotes = request.customerNotes() == null || request.customerNotes().isBlank()
                ? "Direct checkout order from customer account."
                : request.customerNotes().trim();

        PurchaseOrder purchaseOrder = new PurchaseOrder(
                generateOrderNumber(),
                customer.getFullName(),
                customer.getCompanyName(),
                customer.getEmail(),
                customer.getPhone(),
                buildAddressLine(selectedAddress),
                selectedAddress.getCity(),
                selectedAddress.getState(),
                selectedAddress.getPostalCode(),
                customerNotes
        );
        purchaseOrder.setCustomer(customer);

        request.items().stream()
                .map(this::mapOrderItem)
                .forEach(purchaseOrder::addItem);

        applyAutomaticPricing(purchaseOrder, "Customer checkout pricing applied.");
        purchaseOrder.updateStatus(PurchaseOrderStatus.CONFIRMED, "Direct order created from customer account.");
        purchaseOrder.addStatusHistory(new OrderStatusHistory(
                PurchaseOrderStatus.CONFIRMED,
                "Direct order created by authenticated customer."
        ));

        return mapOrder(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return purchaseOrderRepository.findAll().stream()
                .sorted(Comparator.comparing(PurchaseOrder::getCreatedAt).reversed())
                .map(this::mapOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        return purchaseOrderRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapOrder)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return mapOrder(getOrderEntity(id));
    }

    @Transactional(readOnly = true)
    public OrderResponse trackOrder(String orderNumber) {
        return purchaseOrderRepository.findByOrderNumber(orderNumber)
                .map(this::mapOrder)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Transactional
    public OrderResponse quoteOrder(Long id, QuoteOrderRequest request) {
        PurchaseOrder purchaseOrder = getOrderEntity(id);
        Map<Long, QuoteOrderItemRequest> quotedItems = request.items().stream()
                .collect(Collectors.toMap(QuoteOrderItemRequest::itemId, item -> item));

        BigDecimal subtotalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        for (OrderItem item : purchaseOrder.getItems()) {
            QuoteOrderItemRequest quoteItem = quotedItems.get(item.getId());
            if (quoteItem == null) {
                throw new IllegalArgumentException("Missing quote price for order item " + item.getId());
            }

            BigDecimal lineSubtotal = quoteItem.unitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal resolvedDiscountRate = normalizeRate(
                    quoteItem.discountRate() != null ? quoteItem.discountRate() : item.getDiscountRate()
            );
            BigDecimal resolvedTaxRate = normalizeRate(
                    quoteItem.taxRate() != null ? quoteItem.taxRate() : item.getTaxRate()
            );

            BigDecimal lineDiscountAmount = percentageAmount(lineSubtotal, resolvedDiscountRate);
            BigDecimal taxableBase = lineSubtotal.subtract(lineDiscountAmount);
            BigDecimal lineTaxAmount = percentageAmount(taxableBase, resolvedTaxRate);
            BigDecimal lineTotal = taxableBase.add(lineTaxAmount);

            item.applyQuote(
                    quoteItem.unitPrice(),
                    lineSubtotal,
                    resolvedDiscountRate,
                    lineDiscountAmount,
                    resolvedTaxRate,
                    lineTaxAmount,
                    lineTotal
            );
            subtotalAmount = subtotalAmount.add(lineSubtotal);
            taxAmount = taxAmount.add(lineTaxAmount);
            discountAmount = discountAmount.add(lineDiscountAmount);
        }

        if (request.taxAmount() != null) {
            taxAmount = request.taxAmount();
        }
        if (request.discountAmount() != null) {
            discountAmount = request.discountAmount();
        }
        if (taxAmount.compareTo(BigDecimal.ZERO) < 0) {
            taxAmount = BigDecimal.ZERO;
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            discountAmount = BigDecimal.ZERO;
        }
        if (discountAmount.compareTo(subtotalAmount) > 0) {
            discountAmount = subtotalAmount;
        }

        BigDecimal taxableAfterDiscount = subtotalAmount.subtract(discountAmount);
        BigDecimal effectiveTaxRate = subtotalAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : taxAmount.multiply(BigDecimal.valueOf(100))
                .divide(taxableAfterDiscount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : taxableAfterDiscount, 2, RoundingMode.HALF_UP);
        BigDecimal effectiveDiscountRate = subtotalAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : discountAmount.multiply(BigDecimal.valueOf(100)).divide(subtotalAmount, 2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = subtotalAmount
                .subtract(discountAmount)
                .add(request.shippingAmount())
                .add(taxAmount);

        purchaseOrder.applyQuote(
                request.quoteReference(),
                request.adminNotes(),
                subtotalAmount,
                request.shippingAmount(),
                taxAmount,
                discountAmount,
                effectiveTaxRate,
                effectiveDiscountRate,
                totalAmount
        );
        purchaseOrder.addStatusHistory(new OrderStatusHistory(
                purchaseOrder.getStatus(),
                request.adminNotes() == null || request.adminNotes().isBlank()
                        ? "Order pricing updated."
                        : request.adminNotes()
        ));

        return mapOrder(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        PurchaseOrder purchaseOrder = getOrderEntity(id);
        purchaseOrder.updateStatus(request.status(), request.adminNotes());
        purchaseOrder.addStatusHistory(new OrderStatusHistory(
                request.status(),
                request.adminNotes() == null || request.adminNotes().isBlank()
                        ? buildStatusNote(request.status())
                        : request.adminNotes()
        ));
        return mapOrder(purchaseOrderRepository.save(purchaseOrder));
    }

    @Transactional
    public PurchaseOrder markPaymentPending(Long orderId, String provider, String providerOrderId, BigDecimal dueAmount) {
        PurchaseOrder purchaseOrder = getOrderEntity(orderId);
        purchaseOrder.markPaymentPending(provider, providerOrderId, dueAmount);
        purchaseOrder.addStatusHistory(new OrderStatusHistory(purchaseOrder.getStatus(), "Payment session created."));
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder addPaymentStatusHistory(Long orderId, String note) {
        PurchaseOrder purchaseOrder = getOrderEntity(orderId);
        purchaseOrder.addStatusHistory(new OrderStatusHistory(
                purchaseOrder.getStatus(),
                note == null || note.isBlank() ? "Payment gateway update." : note
        ));
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder markPaymentSuccessByProviderOrderId(String providerOrderId, String providerReference) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByPaymentProviderOrderId(providerOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order payment reference not found"));
        purchaseOrder.markPaymentPaid(providerReference);
        purchaseOrder.addStatusHistory(new OrderStatusHistory(purchaseOrder.getStatus(), "Payment confirmed from gateway."));
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder markPaymentFailedByProviderOrderId(String providerOrderId, String providerReference) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByPaymentProviderOrderId(providerOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order payment reference not found"));
        purchaseOrder.markPaymentFailed(providerReference);
        purchaseOrder.addStatusHistory(new OrderStatusHistory(purchaseOrder.getStatus(), "Payment failed from gateway."));
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder findByPaymentProviderOrderId(String providerOrderId) {
        return purchaseOrderRepository.findByPaymentProviderOrderId(providerOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order payment reference not found"));
    }

    private PurchaseOrder getOrderEntity(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderEntityForOperations(Long id) {
        return getOrderEntity(id);
    }

    private OrderItem mapOrderItem(CreateOrderItemRequest request) {
        Product product = productRepository.findBySlug(request.productSlug())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + request.productSlug()));

        String unit = product.getPriceUnit() == null || product.getPriceUnit().isBlank()
                ? "kg"
                : product.getPriceUnit();

        return new OrderItem(
                product,
                product.getName(),
                product.getSlug(),
                request.quantity(),
                unit,
                product.getMoq(),
                normalizeRate(product.getDefaultTaxRate()),
                normalizeRate(product.getDefaultDiscountRate())
        );
    }

    private Customer getOrCreateCustomer(CreateOrderRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = request.phone().trim();
        return customerRepository.findByEmailIgnoreCaseAndPhone(normalizedEmail, normalizedPhone)
                .map(existing -> {
                    existing.updateProfile(
                            request.fullName().trim(),
                            request.companyName().trim(),
                            normalizedEmail,
                            normalizedPhone,
                            request.deliveryAddress().trim(),
                            request.city().trim(),
                            request.state().trim(),
                            request.postalCode().trim()
                    );
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> customerRepository.save(new Customer(
                        request.fullName().trim(),
                        request.companyName().trim(),
                        normalizedEmail,
                        normalizedPhone,
                        request.deliveryAddress().trim(),
                        request.city().trim(),
                        request.state().trim(),
                        request.postalCode().trim()
                )));
    }

    private CustomerAddress resolveAddress(Customer customer, Long addressId) {
        if (addressId != null) {
            return customerAddressRepository.findByIdAndCustomer_Id(addressId, customer.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected address not found"));
        }

        List<CustomerAddress> addresses = customerAddressRepository.findByCustomer_IdOrderByIsDefaultDescUpdatedAtDesc(customer.getId());
        if (!addresses.isEmpty()) {
            return addresses.get(0);
        }

        CustomerAddress address = new CustomerAddress(
                customer,
                "Default",
                customer.getFullName(),
                customer.getPhone() == null ? "" : customer.getPhone(),
                customer.getDeliveryAddress() == null ? "" : customer.getDeliveryAddress(),
                null,
                customer.getCity() == null ? "" : customer.getCity(),
                customer.getState() == null ? "" : customer.getState(),
                customer.getPostalCode() == null ? "" : customer.getPostalCode(),
                "India",
                true
        );
        return customerAddressRepository.save(address);
    }

    private String buildAddressLine(CustomerAddress address) {
        if (address.getLine2() == null || address.getLine2().isBlank()) {
            return address.getLine1();
        }
        return address.getLine1() + ", " + address.getLine2();
    }

    private void applyAutomaticPricing(PurchaseOrder purchaseOrder, String adminNote) {
        BigDecimal subtotalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (OrderItem item : purchaseOrder.getItems()) {
            BigDecimal unitPrice = item.getProduct() != null && item.getProduct().getPrice() != null
                    ? item.getProduct().getPrice()
                    : BigDecimal.ZERO;
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineDiscountAmount = percentageAmount(lineSubtotal, normalizeRate(item.getDiscountRate()));
            BigDecimal taxableBase = lineSubtotal.subtract(lineDiscountAmount);
            BigDecimal lineTaxAmount = percentageAmount(taxableBase, normalizeRate(item.getTaxRate()));
            BigDecimal lineTotal = taxableBase.add(lineTaxAmount);

            item.applyQuote(
                    unitPrice,
                    lineSubtotal,
                    normalizeRate(item.getDiscountRate()),
                    lineDiscountAmount,
                    normalizeRate(item.getTaxRate()),
                    lineTaxAmount,
                    lineTotal
            );

            subtotalAmount = subtotalAmount.add(lineSubtotal);
            discountAmount = discountAmount.add(lineDiscountAmount);
            taxAmount = taxAmount.add(lineTaxAmount);
        }

        BigDecimal effectiveDiscountRate = subtotalAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : discountAmount.multiply(BigDecimal.valueOf(100))
                .divide(subtotalAmount, 2, RoundingMode.HALF_UP);
        BigDecimal taxableAfterDiscount = subtotalAmount.subtract(discountAmount);
        BigDecimal effectiveTaxRate = taxableAfterDiscount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : taxAmount.multiply(BigDecimal.valueOf(100))
                .divide(taxableAfterDiscount, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotalAmount.subtract(discountAmount).add(taxAmount);

        purchaseOrder.applyPricing(
                adminNote,
                subtotalAmount,
                BigDecimal.ZERO,
                taxAmount,
                discountAmount,
                effectiveTaxRate,
                effectiveDiscountRate,
                totalAmount
        );
    }

    private BigDecimal normalizeRate(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(100);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentageAmount(BigDecimal base, BigDecimal rate) {
        return base.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT));
        String orderNumber;
        do {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
            orderNumber = "FVP-" + datePrefix + "-" + suffix;
        } while (purchaseOrderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    private String buildStatusNote(PurchaseOrderStatus status) {
        return switch (status) {
            case PENDING_REVIEW -> "Order is pending internal review.";
            case QUOTED -> "Quote prepared for customer review.";
            case CONFIRMED -> "Order confirmed and ready for fulfillment.";
            case PROCESSING -> "Order is being processed.";
            case SHIPPED -> "Order has been shipped.";
            case DELIVERED -> "Order has been delivered.";
            case CANCELLED -> "Order has been cancelled.";
        };
    }

    private OrderResponse mapOrder(PurchaseOrder purchaseOrder) {
        return new OrderResponse(
                purchaseOrder.getId(),
                purchaseOrder.getCustomer() != null ? purchaseOrder.getCustomer().getId() : null,
                purchaseOrder.getOrderNumber(),
                purchaseOrder.getFullName(),
                purchaseOrder.getCompanyName(),
                purchaseOrder.getEmail(),
                purchaseOrder.getPhone(),
                purchaseOrder.getDeliveryAddress(),
                purchaseOrder.getCity(),
                purchaseOrder.getState(),
                purchaseOrder.getPostalCode(),
                purchaseOrder.getCustomerNotes(),
                purchaseOrder.getStatus(),
                purchaseOrder.getCurrency(),
                purchaseOrder.getPaymentStatus(),
                purchaseOrder.getPaymentDueAmount(),
                purchaseOrder.getPaymentProvider(),
                purchaseOrder.getPaymentProviderOrderId(),
                purchaseOrder.getPaymentProviderReference(),
                purchaseOrder.getPaidAt(),
                purchaseOrder.getCreatedAt(),
                purchaseOrder.getQuotedAt(),
                purchaseOrder.getConfirmedAt(),
                purchaseOrder.getShippedAt(),
                purchaseOrder.getDeliveredAt(),
                purchaseOrder.getAdminNotes(),
                purchaseOrder.getQuoteReference(),
                purchaseOrder.getSubtotalAmount(),
                purchaseOrder.getShippingAmount(),
                purchaseOrder.getTaxAmount(),
                purchaseOrder.getDiscountAmount(),
                purchaseOrder.getEffectiveTaxRate(),
                purchaseOrder.getEffectiveDiscountRate(),
                purchaseOrder.getTotalAmount(),
                purchaseOrder.getItems().stream().map(this::mapItem).toList(),
                purchaseOrder.getStatusHistory().stream()
                        .sorted(Comparator.comparing(OrderStatusHistory::getChangedAt).reversed())
                        .map(this::mapHistory)
                        .toList()
        );
    }

    private OrderItemResponse mapItem(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductName(),
                item.getProductSlug(),
                item.getQuantity(),
                item.getUnit(),
                item.getMoqSnapshot(),
                item.getUnitPrice(),
                item.getLineSubtotal(),
                item.getTaxRate(),
                item.getTaxAmount(),
                item.getDiscountRate(),
                item.getDiscountAmount(),
                item.getLineTotal()
        );
    }

    private OrderStatusHistoryResponse mapHistory(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getStatus(),
                history.getNote(),
                history.getChangedAt()
        );
    }
}

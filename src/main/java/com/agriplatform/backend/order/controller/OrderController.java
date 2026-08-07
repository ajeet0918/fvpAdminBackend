package com.agriplatform.backend.order.controller;

import com.agriplatform.backend.*;
import com.agriplatform.backend.auth.controller.*;
import com.agriplatform.backend.auth.dto.*;
import com.agriplatform.backend.auth.service.*;
import com.agriplatform.backend.category.controller.*;
import com.agriplatform.backend.category.model.*;
import com.agriplatform.backend.category.repository.*;
import com.agriplatform.backend.common.controller.*;
import com.agriplatform.backend.config.*;
import com.agriplatform.backend.customer.controller.*;
import com.agriplatform.backend.customer.dto.*;
import com.agriplatform.backend.customer.model.*;
import com.agriplatform.backend.customer.repository.*;
import com.agriplatform.backend.customer.service.*;
import com.agriplatform.backend.document.controller.*;
import com.agriplatform.backend.document.dto.*;
import com.agriplatform.backend.document.model.*;
import com.agriplatform.backend.document.repository.*;
import com.agriplatform.backend.document.service.*;
import com.agriplatform.backend.inquiry.controller.*;
import com.agriplatform.backend.inquiry.dto.*;
import com.agriplatform.backend.inquiry.model.*;
import com.agriplatform.backend.inquiry.repository.*;
import com.agriplatform.backend.inquiry.service.*;
import com.agriplatform.backend.investor.controller.*;
import com.agriplatform.backend.investor.dto.*;
import com.agriplatform.backend.investor.model.*;
import com.agriplatform.backend.investor.repository.*;
import com.agriplatform.backend.investor.service.*;
import com.agriplatform.backend.lead.controller.*;
import com.agriplatform.backend.lead.dto.*;
import com.agriplatform.backend.lead.model.*;
import com.agriplatform.backend.lead.repository.*;
import com.agriplatform.backend.lead.service.*;
import com.agriplatform.backend.order.controller.*;
import com.agriplatform.backend.order.dto.*;
import com.agriplatform.backend.order.model.*;
import com.agriplatform.backend.order.repository.*;
import com.agriplatform.backend.order.service.*;
import com.agriplatform.backend.portal.controller.*;
import com.agriplatform.backend.portal.dto.*;
import com.agriplatform.backend.portal.model.*;
import com.agriplatform.backend.portal.repository.*;
import com.agriplatform.backend.portal.service.*;
import com.agriplatform.backend.product.controller.*;
import com.agriplatform.backend.product.dto.*;
import com.agriplatform.backend.product.model.*;
import com.agriplatform.backend.product.repository.*;
import com.agriplatform.backend.product.service.*;
import com.agriplatform.backend.security.*;
import com.agriplatform.backend.user.controller.*;
import com.agriplatform.backend.user.dto.*;
import com.agriplatform.backend.user.model.*;
import com.agriplatform.backend.user.repository.*;
import com.agriplatform.backend.user.service.*;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.agriplatform.backend.payment.dto.CreateOrderRefundRequest;
import com.agriplatform.backend.payment.dto.OrderRefundResponse;
import com.agriplatform.backend.payment.dto.CompleteManualRefundRequest;
import com.agriplatform.backend.payment.service.OrderRefundService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderRefundService orderRefundService;

    public OrderController(OrderService orderService, OrderRefundService orderRefundService) {
        this.orderService = orderService;
        this.orderRefundService = orderRefundService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/track/{orderNumber}")
    public OrderResponse trackOrder(@PathVariable String orderNumber) {
        return orderService.trackOrder(orderNumber);
    }

    @PostMapping("/{id}/quote")
    public OrderResponse quoteOrder(@PathVariable Long id, @Valid @RequestBody QuoteOrderRequest request) {
        return orderService.quoteOrder(id, request);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request);
    }

    @PostMapping("/{id}/payment")
    public OrderResponse markOfflinePayment(
            @PathVariable Long id,
            @Valid @RequestBody MarkOrderPaymentRequest request,
            Authentication authentication
    ) {
        return orderService.markOfflinePayment(id, request, authentication.getName());
    }

    @PostMapping("/{id}/cancellation-decision")
    public OrderResponse decideCancellation(
            @PathVariable Long id,
            @Valid @RequestBody CancellationDecisionRequest request,
            Authentication authentication
    ) {
        return orderService.decideCancellation(id, request, authentication.getName());
    }

    @PostMapping("/{id}/refunds")
    public OrderRefundResponse createRefund(
            @PathVariable Long id,
            @Valid @RequestBody CreateOrderRefundRequest request,
            Authentication authentication
    ) {
        return orderRefundService.createRefund(id, request, authentication.getName());
    }

    @PostMapping("/{id}/refunds/sync")
    public List<OrderRefundResponse> syncRefunds(@PathVariable Long id) {
        return orderRefundService.syncRefunds(id);
    }

    @PostMapping("/{id}/refunds/{refundId}/complete")
    public OrderRefundResponse completeManualRefund(
            @PathVariable Long id,
            @PathVariable Long refundId,
            @Valid @RequestBody CompleteManualRefundRequest request,
            Authentication authentication
    ) {
        return orderRefundService.completeManualRefund(id, refundId, request, authentication.getName());
    }
}

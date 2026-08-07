package com.agriplatform.backend.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.agriplatform.backend.order.model.OrderPaymentMethod;
import java.util.List;

public record CreateCustomerOrderRequest(
        Long addressId,
        @Size(max = 1600) String customerNotes,
        @Valid @NotEmpty List<CreateOrderItemRequest> items,
        @NotNull OrderPaymentMethod paymentMethod,
        @NotBlank String checkoutSuccessUrl,
        @NotBlank String checkoutFailureUrl
) {
}

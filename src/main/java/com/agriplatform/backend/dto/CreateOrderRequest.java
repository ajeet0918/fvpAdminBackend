package com.agriplatform.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank String fullName,
        @NotBlank String companyName,
        @Email @NotBlank String email,
        @NotBlank String phone,
        @NotBlank String deliveryAddress,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String postalCode,
        @NotBlank @Size(min = 10, max = 1600) String customerNotes,
        @Valid @NotEmpty List<CreateOrderItemRequest> items
) {
}

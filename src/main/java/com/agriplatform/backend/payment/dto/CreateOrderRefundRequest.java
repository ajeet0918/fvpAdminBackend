package com.agriplatform.backend.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import com.agriplatform.backend.payment.model.OrderRefundMethod;

public record CreateOrderRefundRequest(
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @Size(min = 3, max = 100) String note,
        String speed,
        OrderRefundMethod refundMethod
) {
    public CreateOrderRefundRequest(BigDecimal amount, String note, String speed) {
        this(amount, note, speed, null);
    }
}

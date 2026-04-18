package com.agriplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record MarkInvestorPayoutPaidRequest(
        @NotBlank @Size(max = 80) String paymentChannel,
        @NotBlank @Size(max = 120) String transactionReference,
        LocalDateTime paidAt,
        @Size(max = 1200) String notes
) {
}

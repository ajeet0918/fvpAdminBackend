package com.agriplatform.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateInvestorMonthlyReturnRequest(
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal overrideAmount,
        @Size(max = 300) String overrideReason,
        @Size(max = 1200) String notes
) {
}

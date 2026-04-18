package com.agriplatform.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateInvestorPayoutRequest(
        @NotNull Long investorId,
        @NotEmpty List<Long> monthlyReturnIds,
        @Size(max = 1200) String notes
) {
}

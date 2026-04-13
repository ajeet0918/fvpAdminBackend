package com.agriplatform.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateInquiryRequest(
        @NotBlank @Size(max = 40) String status,
        @Size(max = 40) String verificationStatus,
        @Size(max = 40) String paymentStatus,
        @Size(max = 40) String agreementId,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal committedReturnAmount,
        @Size(max = 1200) String farmerActionNote,
        @Size(max = 1200) String hubActionNote,
        @Size(max = 1200) String adminNotes,
        @Size(max = 120) String assignedTo
) {
}

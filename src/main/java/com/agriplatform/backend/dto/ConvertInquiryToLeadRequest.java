package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Size;

public record ConvertInquiryToLeadRequest(
        @Size(max = 1200) String leadNotes,
        @Size(max = 120) String assignedTo
) {
}

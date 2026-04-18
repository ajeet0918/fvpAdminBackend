package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Size;

public record PayoutActionRequest(
        @Size(max = 1200) String notes
) {
}

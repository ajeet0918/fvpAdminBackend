package com.agriplatform.backend.dto;

import jakarta.validation.constraints.Size;

public record ReturnActionRequest(
        @Size(max = 1200) String notes
) {
}

package com.agriplatform.backend.dto;

public record InvestorProfileResponse(
        InvestorAccountResponse investor,
        InvestmentResponse investment
) {
}

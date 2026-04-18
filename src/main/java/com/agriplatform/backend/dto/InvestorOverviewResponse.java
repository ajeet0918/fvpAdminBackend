package com.agriplatform.backend.dto;

import java.util.List;

public record InvestorOverviewResponse(
        List<InvestorAccountResponse> investors,
        List<InvestorAccountResponse> activeInvestors,
        List<InvestmentResponse> investments
) {
}

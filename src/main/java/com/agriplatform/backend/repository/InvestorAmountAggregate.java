package com.agriplatform.backend.repository;

import java.math.BigDecimal;

public interface InvestorAmountAggregate {
    Long getInvestorId();
    BigDecimal getTotal();
}

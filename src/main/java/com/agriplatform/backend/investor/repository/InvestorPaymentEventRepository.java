package com.agriplatform.backend.investor.repository;

import com.agriplatform.backend.investor.model.InvestorPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorPaymentEventRepository extends JpaRepository<InvestorPaymentEvent, Long> {
    boolean existsByEventKey(String eventKey);
}

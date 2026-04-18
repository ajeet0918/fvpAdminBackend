package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.InvestorReceipt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorReceiptRepository extends JpaRepository<InvestorReceipt, Long> {
    boolean existsByReceiptNumber(String receiptNumber);
    Optional<InvestorReceipt> findByPayout_Id(Long payoutId);
    Optional<InvestorReceipt> findByReceiptNumber(String receiptNumber);
    List<InvestorReceipt> findByPayout_InvestorAccount_IdOrderByGeneratedAtDesc(Long investorAccountId);
}

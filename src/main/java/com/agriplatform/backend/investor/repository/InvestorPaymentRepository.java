package com.agriplatform.backend.investor.repository;

import com.agriplatform.backend.investor.model.InvestorPayment;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestorPaymentRepository extends JpaRepository<InvestorPayment, Long> {
    Optional<InvestorPayment> findBySourceInquiryId(Long sourceInquiryId);
    Optional<InvestorPayment> findByMerchantLinkId(String merchantLinkId);
    List<InvestorPayment> findByInvestorAccount_IdOrderByCreatedAtDesc(Long investorAccountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from InvestorPayment p where p.merchantLinkId = :merchantLinkId")
    Optional<InvestorPayment> findLockedByMerchantLinkId(@Param("merchantLinkId") String merchantLinkId);
}

package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.InvestorPayout;
import com.agriplatform.backend.model.InvestorPayoutStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestorPayoutRepository extends JpaRepository<InvestorPayout, Long> {
    boolean existsByPayoutReference(String payoutReference);
    List<InvestorPayout> findByInvestorAccount_IdOrderByCreatedAtDesc(Long investorAccountId);
    List<InvestorPayout> findByStatusOrderByCreatedAtDesc(InvestorPayoutStatus status);

    @Query("""
            select p.investorAccount.id as investorId, coalesce(sum(p.totalAmount), 0) as total
            from InvestorPayout p
            where p.status in :statuses
            group by p.investorAccount.id
            """)
    List<InvestorAmountAggregate> sumTotalAmountByInvestorIdForStatuses(@Param("statuses") List<InvestorPayoutStatus> statuses);
}

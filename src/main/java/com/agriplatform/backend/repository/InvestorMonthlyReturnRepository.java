package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.InvestorMonthlyReturn;
import com.agriplatform.backend.model.InvestorMonthlyReturnStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestorMonthlyReturnRepository extends JpaRepository<InvestorMonthlyReturn, Long> {
    Optional<InvestorMonthlyReturn> findByInvestment_IdAndPeriodYearAndPeriodMonth(Long investmentId, Integer periodYear, Integer periodMonth);
    List<InvestorMonthlyReturn> findByInvestorAccount_IdOrderByPeriodYearDescPeriodMonthDescCreatedAtDesc(Long investorAccountId);
    List<InvestorMonthlyReturn> findByPeriodYearAndPeriodMonthOrderByUpdatedAtDesc(Integer periodYear, Integer periodMonth);
    List<InvestorMonthlyReturn> findByStatusOrderByUpdatedAtDesc(InvestorMonthlyReturnStatus status);
    List<InvestorMonthlyReturn> findByPayout_IdOrderByCreatedAtAsc(Long payoutId);

    @Query("""
            select r.investorAccount.id as investorId, coalesce(sum(r.finalAmount), 0) as total
            from InvestorMonthlyReturn r
            where r.status = :status
            group by r.investorAccount.id
            """)
    List<InvestorAmountAggregate> sumFinalAmountByInvestorIdForStatus(@Param("status") InvestorMonthlyReturnStatus status);
}

package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.Investment;
import com.agriplatform.backend.model.InvestmentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    boolean existsByInvestmentReference(String investmentReference);
    List<Investment> findByInvestorAccount_IdOrderByStartDateDesc(Long investorAccountId);
    List<Investment> findByStatusOrderByCreatedAtDesc(InvestmentStatus status);

    @Query("select i from Investment i join fetch i.investorAccount ia order by i.createdAt desc")
    List<Investment> findAllWithInvestorOrderByCreatedAtDesc();

    @Query("""
            select i from Investment i
            join fetch i.investorAccount ia
            where ia.id = :investorAccountId
            order by i.startDate desc
            """)
    List<Investment> findByInvestorAccountIdWithInvestorOrderByStartDateDesc(@Param("investorAccountId") Long investorAccountId);

    @Query("""
            select i.investorAccount.id as investorId, coalesce(sum(i.principalAmount), 0) as total
            from Investment i
            group by i.investorAccount.id
            """)
    List<InvestorAmountAggregate> sumPrincipalByInvestorId();
}

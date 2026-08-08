package com.agriplatform.backend.investor.repository;

import com.agriplatform.backend.investor.model.InvestorAgreement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorAgreementRepository extends JpaRepository<InvestorAgreement, Long> {
    Optional<InvestorAgreement> findByInvestment_Id(Long investmentId);
    Optional<InvestorAgreement> findByInvestorPayment_Id(Long investorPaymentId);
    Optional<InvestorAgreement> findByInvestorPayment_SourceInquiryId(Long sourceInquiryId);
    Optional<InvestorAgreement> findByAgreementNumber(String agreementNumber);
    List<InvestorAgreement> findByInvestorAccount_IdOrderByCreatedAtDesc(Long investorAccountId);
    boolean existsByAgreementNumber(String agreementNumber);
}

package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.InvestorAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorAccountRepository extends JpaRepository<InvestorAccount, Long> {
    boolean existsByInvestorCode(String investorCode);
    List<InvestorAccount> findAllByOrderByUpdatedAtDesc();
    List<InvestorAccount> findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(String email, String phone);
    Optional<InvestorAccount> findByEmailIgnoreCaseAndPhone(String email, String phone);
}

package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.Inquiry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    boolean existsByReferenceId(String referenceId);
    List<Inquiry> findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(String email, String phone);
}

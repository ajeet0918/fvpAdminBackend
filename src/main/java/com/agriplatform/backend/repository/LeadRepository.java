package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
}

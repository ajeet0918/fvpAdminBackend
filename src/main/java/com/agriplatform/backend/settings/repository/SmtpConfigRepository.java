package com.agriplatform.backend.settings.repository;

import com.agriplatform.backend.settings.model.SmtpConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmtpConfigRepository extends JpaRepository<SmtpConfig, Long> {
    Optional<SmtpConfig> findFirstByOrderByUpdatedAtDesc();
}

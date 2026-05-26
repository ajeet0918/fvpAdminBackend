package com.agriplatform.backend.portal.repository;

import com.agriplatform.backend.portal.model.PortalPasswordToken;
import com.agriplatform.backend.portal.model.PortalPasswordTokenPurpose;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortalPasswordTokenRepository extends JpaRepository<PortalPasswordToken, Long> {
    Optional<PortalPasswordToken> findByTokenHashAndPurpose(String tokenHash, PortalPasswordTokenPurpose purpose);
}

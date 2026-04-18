package com.agriplatform.backend.repository;

import com.agriplatform.backend.model.PortalOtpChallenge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortalOtpChallengeRepository extends JpaRepository<PortalOtpChallenge, Long> {
    List<PortalOtpChallenge> findTop5ByNormalizedIdentifierAndConsumedAtIsNullOrderByCreatedAtDesc(String normalizedIdentifier);
}

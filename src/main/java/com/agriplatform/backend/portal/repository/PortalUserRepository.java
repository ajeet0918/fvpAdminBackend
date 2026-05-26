package com.agriplatform.backend.portal.repository;

import com.agriplatform.backend.portal.model.PortalUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortalUserRepository extends JpaRepository<PortalUser, Long> {
    Optional<PortalUser> findByUsernameIgnoreCase(String username);

    Optional<PortalUser> findBySourceInquiryId(Long sourceInquiryId);

    Optional<PortalUser> findFirstByUsernameIgnoreCaseOrEmailIgnoreCaseOrPhone(
            String username,
            String email,
            String phone
    );
}

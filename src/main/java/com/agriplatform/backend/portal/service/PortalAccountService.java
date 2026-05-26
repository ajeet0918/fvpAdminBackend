package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.inquiry.model.Inquiry;
import com.agriplatform.backend.inquiry.model.InquiryType;
import com.agriplatform.backend.inquiry.repository.InquiryRepository;
import com.agriplatform.backend.portal.dto.PortalAccountInviteResponse;
import com.agriplatform.backend.portal.model.PortalPasswordTokenPurpose;
import com.agriplatform.backend.portal.model.PortalUser;
import com.agriplatform.backend.portal.model.PortalUserStatus;
import com.agriplatform.backend.portal.model.PortalUserType;
import com.agriplatform.backend.portal.repository.PortalUserRepository;
import java.time.Duration;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalAccountService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalAccountService.class);

    private static final Duration ACTIVATION_TOKEN_TTL = Duration.ofDays(7);
    private static final Duration ACTIVE_RESET_TOKEN_TTL = Duration.ofHours(1);

    private final InquiryRepository inquiryRepository;
    private final PortalUserRepository portalUserRepository;
    private final PortalTokenService portalTokenService;
    private final PortalMailService portalMailService;

    public PortalAccountService(
            InquiryRepository inquiryRepository,
            PortalUserRepository portalUserRepository,
            PortalTokenService portalTokenService,
            PortalMailService portalMailService
    ) {
        this.inquiryRepository = inquiryRepository;
        this.portalUserRepository = portalUserRepository;
        this.portalTokenService = portalTokenService;
        this.portalMailService = portalMailService;
    }

    @Transactional
    public PortalAccountInviteResponse createOrResendInvite(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));
        PortalUserType userType = resolveUserType(inquiry.getInquiryType());
        String username = buildUsername(inquiry);

        PortalUser portalUser = portalUserRepository.findBySourceInquiryId(inquiry.getId())
                .or(() -> portalUserRepository.findByUsernameIgnoreCase(username))
                .map(existing -> refreshUser(existing, inquiry, userType))
                .orElseGet(() -> new PortalUser(
                        username,
                        normalizeEmail(inquiry.getEmail()),
                        normalizePhone(inquiry.getPhone()),
                        userType,
                        inquiry.getId()
                ));

        PortalUser saved = portalUserRepository.save(portalUser);
        if (saved.getStatus() == PortalUserStatus.SUSPENDED) {
            throw new IllegalArgumentException("Portal account is suspended");
        }
        PortalPasswordTokenPurpose purpose = saved.getStatus() == PortalUserStatus.ACTIVE
                ? PortalPasswordTokenPurpose.RESET_PASSWORD
                : PortalPasswordTokenPurpose.ACTIVATION;
        Duration ttl = purpose == PortalPasswordTokenPurpose.ACTIVATION ? ACTIVATION_TOKEN_TTL : ACTIVE_RESET_TOKEN_TTL;

        PortalIssuedToken token = portalTokenService.issueToken(saved, purpose, ttl);
        if (purpose == PortalPasswordTokenPurpose.ACTIVATION) {
            portalMailService.sendActivationEmail(saved, token.rawToken());
        } else {
            portalMailService.sendPasswordResetEmail(saved, token.rawToken());
        }

        return new PortalAccountInviteResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getUserType().name(),
                saved.getStatus().name(),
                "Portal email sent."
        );
    }

    private PortalUser refreshUser(PortalUser portalUser, Inquiry inquiry, PortalUserType userType) {
        portalUser.refreshContact(
                normalizeEmail(inquiry.getEmail()),
                normalizePhone(inquiry.getPhone()),
                userType,
                inquiry.getId()
        );
        return portalUser;
    }

    private PortalUserType resolveUserType(InquiryType inquiryType) {
        if (inquiryType == InquiryType.FARMER) {
            return PortalUserType.FARMER;
        }
        if (inquiryType == InquiryType.INVESTOR) {
            return PortalUserType.INVESTOR;
        }
        if (inquiryType == InquiryType.COLLECTION_HUB) {
            return PortalUserType.COLLECTION_HUB;
        }
        throw new IllegalArgumentException("Portal account can only be created for farmer, investor, or collection hub inquiries");
    }

    private String buildUsername(Inquiry inquiry) {
        String email = normalizeEmail(inquiry.getEmail());
        if (hasText(email)) {
            return email;
        }
        String phone = normalizePhone(inquiry.getPhone());
        if (hasText(phone)) {
            return phone;
        }
        throw new IllegalArgumentException("Inquiry must have email or phone to create portal account");
    }

    private String normalizeEmail(String value) {
        if (!hasText(value)) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String value) {
        if (!hasText(value)) {
            return "";
        }
        return value.replaceAll("\\s+", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

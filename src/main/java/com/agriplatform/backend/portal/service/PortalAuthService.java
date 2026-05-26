package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.auth.service.JwtService;
import com.agriplatform.backend.portal.dto.PortalAuthResponse;
import com.agriplatform.backend.portal.dto.PortalMessageResponse;
import com.agriplatform.backend.portal.model.PortalPasswordToken;
import com.agriplatform.backend.portal.model.PortalPasswordTokenPurpose;
import com.agriplatform.backend.portal.model.PortalUser;
import com.agriplatform.backend.portal.model.PortalUserStatus;
import com.agriplatform.backend.portal.repository.PortalUserRepository;
import java.time.Duration;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalAuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalAuthService.class);

    private static final String PORTAL_ROLE = "PORTAL_USER";
    private static final String TOKEN_TYPE = "Bearer";
    private static final String PORTAL_SUBJECT_PREFIX = "PORTAL:";
    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(1);

    private final PortalUserRepository portalUserRepository;
    private final PortalTokenService portalTokenService;
    private final PortalMailService portalMailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public PortalAuthService(
            PortalUserRepository portalUserRepository,
            PortalTokenService portalTokenService,
            PortalMailService portalMailService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.portalUserRepository = portalUserRepository;
        this.portalTokenService = portalTokenService;
        this.portalMailService = portalMailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public PortalAuthResponse login(String usernameInput, String password) {
        PortalUser portalUser = findByIdentifier(usernameInput);
        if (portalUser.getStatus() != PortalUserStatus.ACTIVE || !passwordEncoder.matches(password, portalUser.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        portalUser.markLogin();
        portalUserRepository.save(portalUser);

        String token = jwtService.generateToken(PORTAL_SUBJECT_PREFIX + portalUser.getId(), PORTAL_ROLE);
        return new PortalAuthResponse(
                token,
                TOKEN_TYPE,
                jwtService.getExpirationSeconds(),
                PORTAL_ROLE,
                portalUser.getUsername(),
                portalUser.getUserType().name()
        );
    }

    @Transactional
    public PortalMessageResponse activate(String rawToken, String password) {
        PortalPasswordToken token = portalTokenService.consumeToken(rawToken, PortalPasswordTokenPurpose.ACTIVATION);
        PortalUser portalUser = token.getPortalUser();
        portalUser.activate(passwordEncoder.encode(password));
        portalUserRepository.save(portalUser);
        return new PortalMessageResponse("Portal account activated. You can now login.");
    }

    @Transactional
    public PortalMessageResponse requestPasswordReset(String identifierInput) {
        portalUserRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCaseOrPhone(
                normalizeIdentifier(identifierInput),
                normalizeIdentifier(identifierInput),
                normalizePhone(identifierInput)
        ).ifPresent(portalUser -> {
            PortalIssuedToken token = portalTokenService.issueToken(
                    portalUser,
                    PortalPasswordTokenPurpose.RESET_PASSWORD,
                    RESET_TOKEN_TTL
            );
            portalMailService.sendPasswordResetEmail(portalUser, token.rawToken());
        });
        return new PortalMessageResponse("If the account exists, a reset email has been sent.");
    }

    @Transactional
    public PortalMessageResponse resetPassword(String rawToken, String password) {
        PortalPasswordToken token = portalTokenService.consumeToken(rawToken, PortalPasswordTokenPurpose.RESET_PASSWORD);
        PortalUser portalUser = token.getPortalUser();
        portalUser.activate(passwordEncoder.encode(password));
        portalUserRepository.save(portalUser);
        return new PortalMessageResponse("Password updated. You can now login.");
    }

    private PortalUser findByIdentifier(String input) {
        String normalized = normalizeIdentifier(input);
        return portalUserRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCaseOrPhone(
                        normalized,
                        normalized,
                        normalizePhone(input)
                )
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
    }

    private String normalizeIdentifier(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        String value = input.trim();
        if (value.contains("@")) {
            return value.toLowerCase(Locale.ROOT);
        }
        return value.replaceAll("\\s+", "");
    }

    private String normalizePhone(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("\\s+", "");
    }
}

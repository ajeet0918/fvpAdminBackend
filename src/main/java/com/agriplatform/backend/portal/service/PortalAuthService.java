package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.auth.service.JwtService;
import com.agriplatform.backend.portal.dto.PortalAuthResponse;
import com.agriplatform.backend.portal.dto.PortalMessageResponse;
import com.agriplatform.backend.portal.model.PortalPasswordToken;
import com.agriplatform.backend.portal.model.PortalPasswordTokenPurpose;
import com.agriplatform.backend.portal.model.PortalUser;
import com.agriplatform.backend.portal.model.PortalUserStatus;
import com.agriplatform.backend.portal.repository.PortalUserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalAuthService {
    private static final Logger log = LoggerFactory.getLogger(PortalAuthService.class);

    private static final String PORTAL_ROLE = "PORTAL_USER";
    private static final String TOKEN_TYPE = "Bearer";
    private static final String PORTAL_SUBJECT_PREFIX = "PORTAL:";
    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(1);
    private static final String PASSWORD_RECOVERY_MESSAGE = "If an account exists, password recovery instructions have been sent.";
    private static final String TEMPORARY_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final int TEMPORARY_PASSWORD_LENGTH = 14;

    private final PortalUserRepository portalUserRepository;
    private final PortalTokenService portalTokenService;
    private final PortalMailService portalMailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

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
                portalUser.getUserType().name(),
                portalUser.isResetPassword()
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
            if (portalUser.getStatus() != PortalUserStatus.ACTIVE) {
                return;
            }

            String temporaryPassword = generateTemporaryPassword();
            portalUser.setTemporaryPassword(passwordEncoder.encode(temporaryPassword));
            portalUserRepository.save(portalUser);

            try {
                portalMailService.sendTemporaryPasswordEmail(portalUser, temporaryPassword);
            } catch (RuntimeException ex) {
                log.warn("Unable to send portal password recovery email for portal user id {}", portalUser.getId(), ex);
            }
        });
        return new PortalMessageResponse(PASSWORD_RECOVERY_MESSAGE);
    }

    @Transactional
    public PortalMessageResponse resetPassword(String rawToken, String password) {
        PortalPasswordToken token = portalTokenService.consumeToken(rawToken, PortalPasswordTokenPurpose.RESET_PASSWORD);
        PortalUser portalUser = token.getPortalUser();
        portalUser.activate(passwordEncoder.encode(password));
        portalUserRepository.save(portalUser);
        return new PortalMessageResponse("Password updated. You can now login.");
    }

    @Transactional
    public PortalMessageResponse changeAuthenticatedPassword(String subject, String password) {
        PortalUser portalUser = findBySubject(subject);
        if (portalUser.getStatus() != PortalUserStatus.ACTIVE) {
            throw new IllegalArgumentException("Portal account is not active");
        }
        portalUser.changePassword(passwordEncoder.encode(password));
        portalUserRepository.save(portalUser);
        return new PortalMessageResponse("Password updated.");
    }

    public boolean isPasswordResetRequired(String subject) {
        return findBySubject(subject).isResetPassword();
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

    private PortalUser findBySubject(String subject) {
        if (subject == null || !subject.startsWith(PORTAL_SUBJECT_PREFIX)) {
            throw new IllegalArgumentException("Invalid portal token");
        }

        try {
            Long portalUserId = Long.parseLong(subject.substring(PORTAL_SUBJECT_PREFIX.length()));
            return portalUserRepository.findById(portalUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Portal user not found"));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid portal token", ex);
        }
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

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
        for (int index = 0; index < TEMPORARY_PASSWORD_LENGTH; index++) {
            int charIndex = secureRandom.nextInt(TEMPORARY_PASSWORD_CHARS.length());
            password.append(TEMPORARY_PASSWORD_CHARS.charAt(charIndex));
        }
        return password.toString();
    }
}

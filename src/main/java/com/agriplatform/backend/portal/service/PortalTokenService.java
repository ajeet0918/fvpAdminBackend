package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.portal.model.PortalPasswordToken;
import com.agriplatform.backend.portal.model.PortalPasswordTokenPurpose;
import com.agriplatform.backend.portal.model.PortalUser;
import com.agriplatform.backend.portal.repository.PortalPasswordTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class PortalTokenService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalTokenService.class);

    private static final int TOKEN_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final SecureRandom secureRandom = new SecureRandom();
    private final PortalPasswordTokenRepository tokenRepository;

    public PortalTokenService(PortalPasswordTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public PortalIssuedToken issueToken(PortalUser portalUser, PortalPasswordTokenPurpose purpose, Duration ttl) {
        String rawToken = generateRawToken();
        PortalPasswordToken token = new PortalPasswordToken(
                portalUser,
                hashToken(rawToken),
                purpose,
                LocalDateTime.now().plus(ttl)
        );
        tokenRepository.save(token);
        return new PortalIssuedToken(rawToken);
    }

    public PortalPasswordToken consumeToken(String rawToken, PortalPasswordTokenPurpose purpose) {
        if (!hasText(rawToken)) {
            throw new IllegalArgumentException("Invalid or expired link");
        }
        PortalPasswordToken token = tokenRepository.findByTokenHashAndPurpose(hashToken(rawToken), purpose)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired link"));
        if (!token.isUsable(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired link");
        }
        token.markUsed();
        return tokenRepository.save(token);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash portal token", ex);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

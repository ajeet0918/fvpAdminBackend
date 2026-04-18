package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.PortalOtpRequestResponse;
import com.agriplatform.backend.dto.PortalOtpVerifyResponse;
import com.agriplatform.backend.model.PortalOtpChallenge;
import com.agriplatform.backend.repository.InquiryRepository;
import com.agriplatform.backend.repository.InvestorAccountRepository;
import com.agriplatform.backend.repository.PortalOtpChallengeRepository;
import com.agriplatform.backend.repository.PurchaseOrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalAuthService {

    private final PortalOtpChallengeRepository portalOtpChallengeRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InquiryRepository inquiryRepository;
    private final InvestorAccountRepository investorAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long otpExpirationSeconds;
    private final boolean devMode;

    public PortalAuthService(
            PortalOtpChallengeRepository portalOtpChallengeRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            InquiryRepository inquiryRepository,
            InvestorAccountRepository investorAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.portal.otp.expiration-seconds:300}") long otpExpirationSeconds,
            @Value("${app.portal.otp.dev-mode:true}") boolean devMode
    ) {
        this.portalOtpChallengeRepository = portalOtpChallengeRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inquiryRepository = inquiryRepository;
        this.investorAccountRepository = investorAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpExpirationSeconds = otpExpirationSeconds;
        this.devMode = devMode;
    }

    @Transactional
    public PortalOtpRequestResponse requestOtp(String identifierInput) {
        String normalizedIdentifier = normalizeIdentifier(identifierInput);
        ensureIdentifierExists(normalizedIdentifier);

        String otp = generateOtp();
        PortalOtpChallenge challenge = new PortalOtpChallenge(
                identifierInput.trim(),
                normalizedIdentifier,
                passwordEncoder.encode(otp),
                LocalDateTime.now().plusSeconds(otpExpirationSeconds)
        );
        portalOtpChallengeRepository.save(challenge);

        return new PortalOtpRequestResponse(
                "OTP generated successfully. Use it to complete login.",
                otpExpirationSeconds,
                devMode ? otp : null
        );
    }

    @Transactional
    public PortalOtpVerifyResponse verifyOtp(String identifierInput, String otp) {
        String normalizedIdentifier = normalizeIdentifier(identifierInput);
        PortalOtpChallenge challenge = findActiveChallenge(normalizedIdentifier);
        if (challenge == null) {
            throw new IllegalArgumentException("OTP expired or not found. Please request a new OTP.");
        }
        if (!passwordEncoder.matches(otp.trim(), challenge.getOtpHash())) {
            challenge.markAttempt();
            portalOtpChallengeRepository.save(challenge);
            throw new IllegalArgumentException("Invalid OTP");
        }

        challenge.consume();
        portalOtpChallengeRepository.save(challenge);

        String token = jwtService.generateToken(normalizedIdentifier, "PORTAL_USER");
        return new PortalOtpVerifyResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                "PORTAL_USER"
        );
    }

    private PortalOtpChallenge findActiveChallenge(String normalizedIdentifier) {
        List<PortalOtpChallenge> challenges = portalOtpChallengeRepository
                .findTop5ByNormalizedIdentifierAndConsumedAtIsNullOrderByCreatedAtDesc(normalizedIdentifier);
        LocalDateTime now = LocalDateTime.now();
        return challenges.stream()
                .filter(item -> item.getConsumedAt() == null)
                .filter(item -> item.getExpiresAt().isAfter(now))
                .filter(item -> item.getAttemptCount() < 5)
                .findFirst()
                .orElse(null);
    }

    private void ensureIdentifierExists(String normalizedIdentifier) {
        boolean exists = !purchaseOrderRepository.findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(normalizedIdentifier, normalizedIdentifier).isEmpty()
                || !inquiryRepository.findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(normalizedIdentifier, normalizedIdentifier).isEmpty()
                || !investorAccountRepository.findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(normalizedIdentifier, normalizedIdentifier).isEmpty();
        if (!exists) {
            throw new IllegalArgumentException("Identifier not found in records");
        }
    }

    private String normalizeIdentifier(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }
        String normalized = input.trim();
        if (normalized.contains("@")) {
            return normalized.toLowerCase(Locale.ROOT);
        }
        return normalized.replaceAll("\\s+", "");
    }

    private String generateOtp() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(value);
    }
}

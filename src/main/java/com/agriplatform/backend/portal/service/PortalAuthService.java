package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.*;
import com.agriplatform.backend.auth.controller.*;
import com.agriplatform.backend.auth.dto.*;
import com.agriplatform.backend.auth.service.*;
import com.agriplatform.backend.category.controller.*;
import com.agriplatform.backend.category.model.*;
import com.agriplatform.backend.category.repository.*;
import com.agriplatform.backend.common.controller.*;
import com.agriplatform.backend.config.*;
import com.agriplatform.backend.customer.controller.*;
import com.agriplatform.backend.customer.dto.*;
import com.agriplatform.backend.customer.model.*;
import com.agriplatform.backend.customer.repository.*;
import com.agriplatform.backend.customer.service.*;
import com.agriplatform.backend.document.controller.*;
import com.agriplatform.backend.document.dto.*;
import com.agriplatform.backend.document.model.*;
import com.agriplatform.backend.document.repository.*;
import com.agriplatform.backend.document.service.*;
import com.agriplatform.backend.inquiry.controller.*;
import com.agriplatform.backend.inquiry.dto.*;
import com.agriplatform.backend.inquiry.model.*;
import com.agriplatform.backend.inquiry.repository.*;
import com.agriplatform.backend.inquiry.service.*;
import com.agriplatform.backend.investor.controller.*;
import com.agriplatform.backend.investor.dto.*;
import com.agriplatform.backend.investor.model.*;
import com.agriplatform.backend.investor.repository.*;
import com.agriplatform.backend.investor.service.*;
import com.agriplatform.backend.lead.controller.*;
import com.agriplatform.backend.lead.dto.*;
import com.agriplatform.backend.lead.model.*;
import com.agriplatform.backend.lead.repository.*;
import com.agriplatform.backend.lead.service.*;
import com.agriplatform.backend.order.controller.*;
import com.agriplatform.backend.order.dto.*;
import com.agriplatform.backend.order.model.*;
import com.agriplatform.backend.order.repository.*;
import com.agriplatform.backend.order.service.*;
import com.agriplatform.backend.portal.controller.*;
import com.agriplatform.backend.portal.dto.*;
import com.agriplatform.backend.portal.model.*;
import com.agriplatform.backend.portal.repository.*;
import com.agriplatform.backend.portal.service.*;
import com.agriplatform.backend.product.controller.*;
import com.agriplatform.backend.product.dto.*;
import com.agriplatform.backend.product.model.*;
import com.agriplatform.backend.product.repository.*;
import com.agriplatform.backend.product.service.*;
import com.agriplatform.backend.security.*;
import com.agriplatform.backend.user.controller.*;
import com.agriplatform.backend.user.dto.*;
import com.agriplatform.backend.user.model.*;
import com.agriplatform.backend.user.repository.*;
import com.agriplatform.backend.user.service.*;

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

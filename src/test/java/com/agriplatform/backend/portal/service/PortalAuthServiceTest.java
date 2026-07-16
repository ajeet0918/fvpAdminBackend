package com.agriplatform.backend.portal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.auth.service.JwtService;
import com.agriplatform.backend.portal.dto.PortalAuthResponse;
import com.agriplatform.backend.portal.dto.PortalMessageResponse;
import com.agriplatform.backend.portal.model.PortalUser;
import com.agriplatform.backend.portal.model.PortalUserType;
import com.agriplatform.backend.portal.repository.PortalUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PortalAuthServiceTest {
    private static final String RECOVERY_MESSAGE = "If an account exists, password recovery instructions have been sent.";

    @Mock
    private PortalUserRepository portalUserRepository;

    @Mock
    private PortalTokenService portalTokenService;

    @Mock
    private PortalMailService portalMailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PortalAuthService portalAuthService;

    @Test
    void requestPasswordResetForUnknownAccountReturnsGenericMessage() {
        when(portalUserRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCaseOrPhone(
                "unknown@example.com",
                "unknown@example.com",
                "unknown@example.com"
        )).thenReturn(Optional.empty());

        PortalMessageResponse response = portalAuthService.requestPasswordReset("unknown@example.com");

        assertThat(response.message()).isEqualTo(RECOVERY_MESSAGE);
        verify(portalUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(portalMailService, never()).sendTemporaryPasswordEmail(org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    void requestPasswordResetForActiveAccountSetsTemporaryPasswordAndEmailsRegisteredAddress() {
        PortalUser portalUser = activePortalUser();
        when(portalUserRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCaseOrPhone(
                "partner@example.com",
                "partner@example.com",
                "partner@example.com"
        )).thenReturn(Optional.of(portalUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temporary-password");

        PortalMessageResponse response = portalAuthService.requestPasswordReset("partner@example.com");

        assertThat(response.message()).isEqualTo(RECOVERY_MESSAGE);
        assertThat(portalUser.isResetPassword()).isTrue();
        assertThat(portalUser.getPasswordHash()).isEqualTo("encoded-temporary-password");
        verify(portalUserRepository).save(portalUser);
        verify(portalMailService).sendTemporaryPasswordEmail(
                eq(portalUser),
                argThat(temporaryPassword -> temporaryPassword != null && temporaryPassword.length() == 14)
        );
    }

    @Test
    void loginIncludesResetPasswordFlagWhenTemporaryPasswordIsActive() {
        PortalUser portalUser = activePortalUser();
        portalUser.setTemporaryPassword("encoded-temporary-password");
        when(portalUserRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCaseOrPhone(
                "partner",
                "partner",
                "partner"
        )).thenReturn(Optional.of(portalUser));
        when(passwordEncoder.matches("temporary-password", "encoded-temporary-password")).thenReturn(true);
        when(jwtService.generateToken("PORTAL:null", "PORTAL_USER")).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        PortalAuthResponse response = portalAuthService.login("partner", "temporary-password");

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.resetPassword()).isTrue();
    }

    private PortalUser activePortalUser() {
        PortalUser portalUser = new PortalUser(
                "partner",
                "partner@example.com",
                "9999999999",
                PortalUserType.FARMER,
                7L
        );
        portalUser.activate("encoded-password");
        return portalUser;
    }
}

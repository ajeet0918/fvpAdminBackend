package com.agriplatform.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SIGNING_SECRET = "0123456789abcdef0123456789abcdef";
    private static final String USERNAME = "admin@example.com";
    private static final String ROLE = "ADMIN";

    @Test
    void generateTokenCreatesValidTokenWithExpectedClaims() {
        JwtService jwtService = new JwtService(SIGNING_SECRET, 60_000);

        String token = jwtService.generateToken(USERNAME, ROLE);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(USERNAME);
        assertThat(jwtService.extractRole(token)).isEqualTo(ROLE);
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(60);
    }

    @Test
    void isTokenValidRejectsExpiredToken() {
        JwtService jwtService = new JwtService(SIGNING_SECRET, -1_000);

        String token = jwtService.generateToken(USERNAME, ROLE);

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValidRejectsTamperedToken() {
        JwtService jwtService = new JwtService(SIGNING_SECRET, 60_000);
        String token = jwtService.generateToken(USERNAME, ROLE);

        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isTokenValid(tamperedToken)).isFalse();
    }
}

package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.LoginRequest;
import com.agriplatform.backend.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            log.warn(
                    "Login authentication failed. username={}, reason={}",
                    request.username(),
                    ex.getClass().getSimpleName()
            );
            throw new BadCredentialsException("Invalid username or password");
        }

        String roleCode = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Login failed: authenticated principal has no role authority. username={}", request.username());
                    return new AuthenticationCredentialsNotFoundException("Role not found");
                });
        String token = jwtService.generateToken(authentication.getName(), roleCode);
        log.info("Login success. username={}, role={}", authentication.getName(), roleCode);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(), roleCode);
    }
}

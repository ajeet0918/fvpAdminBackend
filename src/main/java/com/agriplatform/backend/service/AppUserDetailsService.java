package com.agriplatform.backend.service;

import com.agriplatform.backend.model.AppUser;
import com.agriplatform.backend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AppUserDetailsService.class);

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found. username={}", username);
                    return new UsernameNotFoundException("Invalid username or password");
                });

        if (!user.isActive() || user.getRole() == null) {
            log.warn(
                    "Login failed: inactive user or missing role. username={}, active={}, hasRole={}",
                    username,
                    user.isActive(),
                    user.getRole() != null
            );
            throw new BadCredentialsException("Invalid username or password");
        }

        log.info("Login principal loaded. username={}, role={}", username, user.getRole().getCode());
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().getCode())
                .disabled(!user.isActive())
                .build();
    }
}

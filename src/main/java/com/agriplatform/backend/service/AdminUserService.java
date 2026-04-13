package com.agriplatform.backend.service;

import com.agriplatform.backend.dto.AdminRoleResponse;
import com.agriplatform.backend.dto.AdminUserResponse;
import com.agriplatform.backend.dto.AssignableOwnerResponse;
import com.agriplatform.backend.dto.CreateAdminUserRequest;
import com.agriplatform.backend.dto.ResetPasswordRequest;
import com.agriplatform.backend.dto.UpdateAdminUserRequest;
import com.agriplatform.backend.model.AppRole;
import com.agriplatform.backend.model.AppUser;
import com.agriplatform.backend.repository.AppRoleRepository;
import com.agriplatform.backend.repository.AppUserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final AppRoleRepository appRoleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            AppRoleRepository appRoleRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appRoleRepository = appRoleRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminRoleResponse> getRoles() {
        return appRoleRepository.findAll().stream()
                .sorted(Comparator.comparing(AppRole::getCode))
                .map(role -> new AdminRoleResponse(role.getId(), role.getCode(), role.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers(String search, String status, String roleCode) {
        String searchFilter = normalizeSearch(search);
        Boolean activeFilter = parseActiveFilter(status);
        String roleFilter = normalizeUpperNullable(roleCode);

        return appUserRepository.findAll().stream()
                .filter(user -> matchesSearch(user, searchFilter))
                .filter(user -> activeFilter == null || user.isActive() == activeFilter)
                .filter(user -> roleFilter == null || (user.getRole() != null && roleFilter.equals(user.getRole().getCode())))
                .sorted(Comparator.comparing(AppUser::getCreatedAt).reversed())
                .map(this::mapUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignableOwnerResponse> getAssignableOwners() {
        String currentRole = getCurrentRole();
        String currentUsername = getCurrentUsername();

        if ("SALES".equals(currentRole)) {
            AppUser user = appUserRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return List.of(new AssignableOwnerResponse(
                    user.getId(),
                    user.getUsername(),
                    buildDisplayName(user)
            ));
        }

        return appUserRepository.findByActiveTrueAndRole_CodeOrderByUsernameAsc("SALES").stream()
                .map(user -> new AssignableOwnerResponse(
                        user.getId(),
                        user.getUsername(),
                        buildDisplayName(user)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapUser(user);
    }

    @Transactional
    public AdminUserResponse createUser(CreateAdminUserRequest request) {
        String username = request.username().trim();
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        String email = request.email().trim().toLowerCase();
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppRole role = appRoleRepository.findByCode(request.roleCode().trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role code"));

        AppUser user = new AppUser(
                username,
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                normalizePhone(request.phone()),
                passwordEncoder.encode(request.password()),
                role
        );
        user.setActive(request.active());

        return mapUser(appUserRepository.save(user));
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, UpdateAdminUserRequest request) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String email = request.email().trim().toLowerCase();
        if (appUserRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppRole role = appRoleRepository.findByCode(request.roleCode().trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role code"));

        user.assignRole(role);
        user.setActive(request.active());
        user.updateProfile(
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                normalizePhone(request.phone())
        );

        return mapUser(appUserRepository.save(user));
    }

    @Transactional
    public AdminUserResponse resetPassword(Long id, ResetPasswordRequest request) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return mapUser(appUserRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        appUserRepository.deleteById(id);
    }

    private AdminUserResponse mapUser(AppUser user) {
        String roleCode = user.getRole() != null ? user.getRole().getCode() : "";
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                user.isActive() ? "ACTIVE" : "INACTIVE",
                user.getCreatedAt(),
                roleCode,
                roleName
        );
    }

    private String normalizePhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String normalizeUpperNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private boolean matchesSearch(AppUser user, String search) {
        if (search == null) {
            return true;
        }
        return containsIgnoreCase(user.getUsername(), search)
                || containsIgnoreCase(user.getFirstName(), search)
                || containsIgnoreCase(user.getLastName(), search)
                || containsIgnoreCase(user.getEmail(), search)
                || containsIgnoreCase(user.getPhone(), search);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private Boolean parseActiveFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if ("ACTIVE".equals(normalized)) {
            return true;
        }
        if ("INACTIVE".equals(normalized)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid user status filter");
    }

    private String buildDisplayName(AppUser user) {
        String combined = ((user.getFirstName() == null ? "" : user.getFirstName().trim())
                + " "
                + (user.getLastName() == null ? "" : user.getLastName().trim()))
                .trim();
        return combined.isBlank() ? user.getUsername() : combined;
    }

    private String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse("");
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "";
    }
}

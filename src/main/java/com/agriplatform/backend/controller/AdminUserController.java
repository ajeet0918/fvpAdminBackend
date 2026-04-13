package com.agriplatform.backend.controller;

import com.agriplatform.backend.dto.AdminRoleResponse;
import com.agriplatform.backend.dto.AdminUserResponse;
import com.agriplatform.backend.dto.AssignableOwnerResponse;
import com.agriplatform.backend.dto.CreateAdminUserRequest;
import com.agriplatform.backend.dto.ResetPasswordRequest;
import com.agriplatform.backend.dto.UpdateAdminUserRequest;
import com.agriplatform.backend.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/roles")
    public List<AdminRoleResponse> getRoles() {
        return adminUserService.getRoles();
    }

    @GetMapping("/owners")
    public List<AssignableOwnerResponse> getAssignableOwners() {
        return adminUserService.getAssignableOwners();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleCode
    ) {
        return adminUserService.getUsers(search, status, roleCode);
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUser(@PathVariable Long id) {
        return adminUserService.getUser(id);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createUser(@Valid @RequestBody CreateAdminUserRequest request) {
        return adminUserService.createUser(request);
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateAdminUserRequest request) {
        return adminUserService.updateUser(id, request);
    }

    @PostMapping("/users/{id}/reset-password")
    public AdminUserResponse resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        return adminUserService.resetPassword(id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
    }
}

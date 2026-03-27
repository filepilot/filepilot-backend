package com.filepilot.vcs.controller;

import com.filepilot.vcs.dto.request.UpdateRoleRequest;
import com.filepilot.vcs.dto.response.UserResponse;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.security.SecurityUtil;
import com.filepilot.vcs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        User admin = securityUtil.getCurrentUser();
        return ResponseEntity.ok(userService.updateRole(id, request.getRole(), admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User admin = securityUtil.getCurrentUser();
        userService.deleteUser(id, admin);
        return ResponseEntity.noContent().build();
    }
}

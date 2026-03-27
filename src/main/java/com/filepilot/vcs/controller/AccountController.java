package com.filepilot.vcs.controller;

import com.filepilot.vcs.dto.request.ChangePasswordRequest;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.security.SecurityUtil;
import com.filepilot.vcs.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = securityUtil.getCurrentUser();
        authService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }
}

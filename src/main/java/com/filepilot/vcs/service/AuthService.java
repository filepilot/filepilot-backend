package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.request.ChangePasswordRequest;
import com.filepilot.vcs.dto.request.LoginRequest;
import com.filepilot.vcs.dto.request.RegisterRequest;
import com.filepilot.vcs.dto.response.AuthResponse;
import com.filepilot.vcs.exception.InvalidOperationException;
import com.filepilot.vcs.mapper.DocumentMapper;
import com.filepilot.vcs.model.Role;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.repository.UserRepository;
import com.filepilot.vcs.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    // A precomputed bcrypt hash of a random string. Used to spend the same CPU
    // when login is attempted against a missing username, so response timing
    // doesn't reveal whether an account exists.
    private static final String DUMMY_BCRYPT_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final DocumentMapper mapper;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Generic message to avoid revealing which of (username, email) is taken.
        if (userRepository.existsByUsername(request.getUsername())
                || userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidOperationException("Registration failed. Please choose different credentials.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.READER);

        User saved = userRepository.save(user);

        auditService.log(saved, "USER_REGISTERED", "USER", saved.getId(),
                "New user registered: " + saved.getUsername());

        String token = jwtUtil.generateToken(saved);
        return mapper.toAuthResponse(saved, token);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new InvalidOperationException("New password must be different from current password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("New password and confirmation do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.log(user, "PASSWORD_CHANGED", "USER", user.getId(),
                "Password changed for user: " + user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        // Always run bcrypt — even when the user doesn't exist — so missing-account
        // responses take the same time as wrong-password responses.
        String hashToCheck = (user != null) ? user.getPasswordHash() : DUMMY_BCRYPT_HASH;
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), hashToCheck);

        if (user == null || !passwordMatches) {
            if (user != null) {
                auditService.log(user, "LOGIN_FAILED", "USER", user.getId(),
                        "Failed login attempt for user: " + user.getUsername());
            }
            throw new InvalidOperationException("Invalid username or password");
        }

        auditService.log(user, "LOGIN_SUCCESS", "USER", user.getId(),
                "User logged in: " + user.getUsername());

        String token = jwtUtil.generateToken(user);
        return mapper.toAuthResponse(user, token);
    }
}

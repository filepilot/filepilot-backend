package com.filepilot.vcs.dto.response;

import com.filepilot.vcs.model.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private String token;
    private LocalDateTime createdAt;
}

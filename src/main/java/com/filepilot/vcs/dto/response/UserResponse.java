package com.filepilot.vcs.dto.response;

import com.filepilot.vcs.model.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
}
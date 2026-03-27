package com.filepilot.vcs.dto.request;

import com.filepilot.vcs.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
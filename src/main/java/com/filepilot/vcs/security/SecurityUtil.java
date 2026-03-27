package com.filepilot.vcs.security;

import com.filepilot.vcs.exception.AccessDeniedException;
import com.filepilot.vcs.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Not authenticated");
        }
        return (User) auth.getPrincipal();
    }
}

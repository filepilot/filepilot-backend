package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.response.UserResponse;
import com.filepilot.vcs.model.Role;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.repository.AuditLogRepository;
import com.filepilot.vcs.repository.CommentRepository;
import com.filepilot.vcs.repository.DocumentRepository;
import com.filepilot.vcs.repository.DocumentVersionRepository;
import com.filepilot.vcs.repository.UserRepository;
import com.filepilot.vcs.mapper.DocumentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdditionalTest {

    @Mock private UserRepository userRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentVersionRepository documentVersionRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private DocumentMapper mapper;
    @Mock private AuditService auditService;

    @InjectMocks private UserService userService;

    @Test
    void findById_returns_user_when_found() {
        User user = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void findById_throws_when_not_found() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.filepilot.vcs.exception.ResourceNotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void findByUsername_returns_user_when_found() {
        User user = mock(User.class);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("bob");
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void findByUsername_throws_when_not_found() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThrows(com.filepilot.vcs.exception.ResourceNotFoundException.class, () -> userService.findByUsername("missing"));
    }

    @Test
    void getAllUsers_maps_to_responses() {
        User u1 = mock(User.class);
        User u2 = mock(User.class);
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        UserResponse r1 = new UserResponse();
        UserResponse r2 = new UserResponse();
        when(mapper.toUserResponse(u1)).thenReturn(r1);
        when(mapper.toUserResponse(u2)).thenReturn(r2);

        List<UserResponse> results = userService.getAllUsers();
        assertEquals(2, results.size());
        assertEquals(r1, results.get(0));
        assertEquals(r2, results.get(1));
    }
}

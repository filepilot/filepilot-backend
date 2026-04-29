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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentVersionRepository documentVersionRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private DocumentMapper mapper;
    @Mock private AuditService auditService;

    @InjectMocks private UserService userService;

    @Test
    void updateRole_admin_can_change_role_success() {
        // Arrange
        User admin = mock(User.class);
        when(admin.getRole()).thenReturn(Role.ADMIN);

        User target = mock(User.class);
        // Target.getId() is not used by updateRole logic in this test; make it lenient to avoid UnnecessaryStubbing
        lenient().when(target.getId()).thenReturn(2L);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(2L);
        userResponse.setUsername("user");
        userResponse.setEmail("user@example.com");
        userResponse.setRole(Role.AUTHOR);

        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // Act
        UserResponse result = userService.updateRole(2L, Role.AUTHOR, admin);

        // Assert
        assertNotNull(result);
        assertEquals(Role.AUTHOR, result.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(target).setRole(eq(Role.AUTHOR));
        verify(auditService).log(eq(admin), eq("USER_ROLE_CHANGED"), eq("USER"), eq(2L), anyString());
    }

    @Test
    void updateRole_non_admin_throws_access_denied() {
        // Arrange
        User nonAdmin = mock(User.class);
        when(nonAdmin.getRole()).thenReturn(Role.READER);

        // Act & Assert
        assertThrows(com.filepilot.vcs.exception.AccessDeniedException.class, () ->
                userService.updateRole(2L, Role.AUTHOR, nonAdmin));
    }
}

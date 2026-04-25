package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.response.UserResponse;
import com.filepilot.vcs.exception.AccessDeniedException;
import com.filepilot.vcs.exception.InvalidOperationException;
import com.filepilot.vcs.exception.ResourceNotFoundException;
import com.filepilot.vcs.mapper.DocumentMapper;
import com.filepilot.vcs.model.Document;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.Role;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.repository.AuditLogRepository;
import com.filepilot.vcs.repository.CommentRepository;
import com.filepilot.vcs.repository.DocumentRepository;
import com.filepilot.vcs.repository.DocumentVersionRepository;
import com.filepilot.vcs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final CommentRepository commentRepository;
    private final AuditLogRepository auditLogRepository;
    private final DocumentMapper mapper;
    private final AuditService auditService;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateRole(Long userId, Role newRole, User admin) {
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can change roles");
        }

        User user = findById(userId);
        Role oldRole = user.getRole();

        // Prevent admin self-demotion: leaves a working admin in place even via direct API calls.
        if (user.getId().equals(admin.getId()) && newRole != Role.ADMIN) {
            throw new InvalidOperationException("Admins cannot change their own role");
        }
        // Refuse to remove the last remaining ADMIN; otherwise the system becomes un-administered.
        if (oldRole == Role.ADMIN && newRole != Role.ADMIN
                && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new InvalidOperationException("Cannot demote the last remaining admin");
        }

        user.setRole(newRole);
        userRepository.save(user);

        auditService.log(admin, "USER_ROLE_CHANGED", "USER", userId,
                "Role changed from " + oldRole + " to " + newRole);

        return mapper.toUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId, User admin) {
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can delete users");
        }

        User user = findById(userId);
        String username = user.getUsername();

        // Same self-protection as updateRole; mirrors the frontend guard so direct API calls cannot lock the system out.
        if (user.getId().equals(admin.getId())) {
            throw new InvalidOperationException("Admins cannot delete themselves");
        }
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new InvalidOperationException("Cannot delete the last remaining admin");
        }

        // Preserve audit history (compliance/forensics): null the FK instead of deleting rows.
        auditLogRepository.clearUserReference(user);

        // Delete comments authored by this user
        commentRepository.deleteByAuthor(user);

        // Clear reviewer references (nullable FK) on document versions
        documentVersionRepository.clearReviewerByUser(user);

        // Delete documents owned by this user. JPA cascades versions, but comments have no
        // DB-level cascade from version_id — wipe them per-version before deleting.
        List<Document> ownedDocuments = documentRepository.findByOwner(user);
        for (Document doc : ownedDocuments) {
            doc.setActiveVersion(null);
            documentRepository.save(doc);
            for (DocumentVersion v : documentVersionRepository.findByDocumentOrderByVersionNumberDesc(doc)) {
                commentRepository.deleteByVersion(v);
            }
        }
        documentRepository.deleteAll(ownedDocuments);

        // Handle versions authored by this user on other people's documents
        List<DocumentVersion> authoredVersions = documentVersionRepository.findByAuthor(user);
        for (DocumentVersion version : authoredVersions) {
            Document doc = version.getDocument();
            if (doc.getActiveVersion() != null && doc.getActiveVersion().getId().equals(version.getId())) {
                doc.setActiveVersion(null);
                documentRepository.save(doc);
            }
            commentRepository.deleteByVersion(version);
        }
        documentVersionRepository.deleteAll(authoredVersions);

        userRepository.delete(user);

        auditService.log(admin, "USER_DELETED", "USER", userId,
                "Deleted user: " + username);
    }
}
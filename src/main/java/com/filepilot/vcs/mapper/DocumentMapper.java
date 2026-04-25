package com.filepilot.vcs.mapper;

import com.filepilot.vcs.dto.response.*;
import com.filepilot.vcs.model.*;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public AuthResponse toAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setToken(token);
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public DocumentResponse toDocumentResponse(Document document) {
        int total = document.getVersions() != null ? document.getVersions().size() : 0;
        return toDocumentResponse(document, total);
    }

    /**
     * Caller-supplied visibleVersionCount avoids leaking hidden DRAFT/PENDING activity
     * to users who can't actually read those versions. See VersionService.canReadVersion.
     */
    public DocumentResponse toDocumentResponse(Document document, int visibleVersionCount) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setTitle(document.getTitle());
        response.setSlug(document.getSlug());
        response.setDescription(document.getDescription());
        response.setOwnerUsername(document.getOwner().getUsername());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());

        if (document.getActiveVersion() != null) {
            response.setActiveVersionId(document.getActiveVersion().getId());
            response.setActiveVersionNumber(document.getActiveVersion().getVersionNumber());
        }

        response.setTotalVersions(visibleVersionCount);

        return response;
    }

    public VersionResponse toVersionResponse(DocumentVersion version) {
        VersionResponse response = new VersionResponse();
        response.setId(version.getId());
        response.setDocumentId(version.getDocument().getId());
        response.setDocumentTitle(version.getDocument().getTitle());
        response.setDocumentSlug(version.getDocument().getSlug());
        response.setVersionNumber(version.getVersionNumber());
        response.setName(version.getName());
        response.setContent(version.getContent());
        response.setStatus(version.getStatus());
        response.setAuthorUsername(version.getAuthor().getUsername());
        response.setReviewComment(version.getReviewComment());
        response.setCreatedAt(version.getCreatedAt());

        if (version.getReviewer() != null) {
            response.setReviewerUsername(version.getReviewer().getUsername());
        }

        return response;
    }

    public CommentResponse toCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setAuthorUsername(comment.getAuthor().getUsername());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    public AuditLogResponse toAuditLogResponse(AuditLog auditLog) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(auditLog.getId());
        response.setAction(auditLog.getAction());
        response.setEntityType(auditLog.getEntityType());
        response.setEntityId(auditLog.getEntityId());
        response.setDetails(auditLog.getDetails());
        response.setCreatedAt(auditLog.getCreatedAt());

        if (auditLog.getUser() != null) {
            response.setUsername(auditLog.getUser().getUsername());
        }

        return response;
    }
}

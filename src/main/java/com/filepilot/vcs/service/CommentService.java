package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.response.CommentResponse;
import com.filepilot.vcs.exception.AccessDeniedException;
import com.filepilot.vcs.mapper.DocumentMapper;
import com.filepilot.vcs.model.*;
import com.filepilot.vcs.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final VersionService versionService;
    private final DocumentMapper mapper;
    private final AuditService auditService;

    public List<CommentResponse> getCommentsByVersion(Long versionId, User user) {
        DocumentVersion version = versionService.findVersionForRead(versionId, user);

        return commentRepository.findByVersionOrderByCreatedAtDesc(version).stream()
                .map(mapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long versionId, String content, User author) {
        if (author.getRole() != Role.REVIEWER && author.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only reviewers and admins can add comments");
        }

        DocumentVersion version = versionService.findVersionForRead(versionId, author);

        Comment comment = new Comment();
        comment.setVersion(version);
        comment.setAuthor(author);
        comment.setContent(content);

        Comment saved = commentRepository.save(comment);

        auditService.log(author, "COMMENT_ADDED", "VERSION", versionId,
                "Added comment on version " + version.getVersionNumber());

        return mapper.toCommentResponse(saved);
    }
}
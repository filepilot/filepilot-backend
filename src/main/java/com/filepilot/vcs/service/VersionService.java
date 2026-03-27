package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.request.CreateVersionRequest;
import com.filepilot.vcs.dto.request.ReviewVersionRequest;
import com.filepilot.vcs.dto.response.VersionResponse;
import com.filepilot.vcs.exception.AccessDeniedException;
import com.filepilot.vcs.exception.InvalidOperationException;
import com.filepilot.vcs.exception.ResourceNotFoundException;
import com.filepilot.vcs.mapper.DocumentMapper;
import com.filepilot.vcs.model.*;
import com.filepilot.vcs.repository.DocumentRepository;
import com.filepilot.vcs.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentMapper mapper;
    private final AuditService auditService;

    public List<VersionResponse> getVersionsByDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        return versionRepository.findByDocumentOrderByVersionNumberDesc(document).stream()
                .map(mapper::toVersionResponse)
                .collect(Collectors.toList());
    }

    public List<VersionResponse> getVersionsByDocumentSlug(String slug) {
        Document document = documentRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + slug));

        return versionRepository.findByDocumentOrderByVersionNumberDesc(document).stream()
                .map(mapper::toVersionResponse)
                .collect(Collectors.toList());
    }

    public VersionResponse getVersionBySlugAndNumber(String slug, Integer versionNumber) {
        Document document = documentRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + slug));

        DocumentVersion version = versionRepository.findByDocumentAndVersionNumber(document, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Version " + versionNumber + " not found for document: " + slug));

        return mapper.toVersionResponse(version);
    }

    public List<VersionResponse> getPendingVersions() {
        return versionRepository.findByStatus(VersionStatus.PENDING_REVIEW).stream()
                .map(mapper::toVersionResponse)
                .collect(Collectors.toList());
    }

    public VersionResponse getVersionById(Long id) {
        DocumentVersion version = findVersionOrThrow(id);
        return mapper.toVersionResponse(version);
    }

    @Transactional
    public VersionResponse createVersion(Long documentId, CreateVersionRequest request, User author) {
        if (author.getRole() != Role.AUTHOR && author.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only authors and admins can create versions");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        DocumentVersion latestVersion = versionRepository
                .findTopByDocumentForUpdate(document)
                .orElse(null);

        int nextVersionNumber = (latestVersion != null) ? latestVersion.getVersionNumber() + 1 : 1;

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setVersionNumber(nextVersionNumber);
        version.setName(request.getName());
        version.setContent(request.getContent());
        version.setStatus(VersionStatus.DRAFT);
        version.setAuthor(author);

        DocumentVersion saved = versionRepository.save(version);

        auditService.log(author, "VERSION_CREATED", "VERSION", saved.getId(),
                "Created version " + nextVersionNumber + " for document: " + document.getTitle());

        return mapper.toVersionResponse(saved);
    }

    @Transactional
    public VersionResponse submitForReview(Long versionId, User author) {
        DocumentVersion version = findVersionOrThrow(versionId);

        if (!version.getAuthor().getId().equals(author.getId()) && author.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only the version author can submit for review");
        }

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new InvalidOperationException("Only DRAFT versions can be submitted for review");
        }

        version.setStatus(VersionStatus.PENDING_REVIEW);
        versionRepository.save(version);

        auditService.log(author, "VERSION_SUBMITTED", "VERSION", versionId,
                "Submitted version " + version.getVersionNumber() + " for review");

        return mapper.toVersionResponse(version);
    }

    @Transactional
    public VersionResponse approveVersion(Long versionId, ReviewVersionRequest request, User reviewer) {
        if (reviewer.getRole() != Role.REVIEWER && reviewer.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only reviewers and admins can approve versions");
        }

        DocumentVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != VersionStatus.PENDING_REVIEW) {
            throw new InvalidOperationException("Only PENDING_REVIEW versions can be approved");
        }

        version.setStatus(VersionStatus.APPROVED);
        version.setReviewer(reviewer);
        if (request != null && request.getComment() != null) {
            version.setReviewComment(request.getComment());
        }
        versionRepository.save(version);

        Document document = version.getDocument();
        document.setActiveVersion(version);
        documentRepository.save(document);

        auditService.log(reviewer, "VERSION_APPROVED", "VERSION", versionId,
                "Approved version " + version.getVersionNumber() + " for document: " + document.getTitle());

        return mapper.toVersionResponse(version);
    }

    @Transactional
    public VersionResponse rejectVersion(Long versionId, ReviewVersionRequest request, User reviewer) {
        if (reviewer.getRole() != Role.REVIEWER && reviewer.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only reviewers and admins can reject versions");
        }

        DocumentVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != VersionStatus.PENDING_REVIEW) {
            throw new InvalidOperationException("Only PENDING_REVIEW versions can be rejected");
        }

        version.setStatus(VersionStatus.REJECTED);
        version.setReviewer(reviewer);
        if (request != null && request.getComment() != null) {
            version.setReviewComment(request.getComment());
        }
        versionRepository.save(version);

        auditService.log(reviewer, "VERSION_REJECTED", "VERSION", versionId,
                "Rejected version " + version.getVersionNumber() + " for document: " + version.getDocument().getTitle());

        return mapper.toVersionResponse(version);
    }

    private DocumentVersion findVersionOrThrow(Long id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found with id: " + id));
    }
}
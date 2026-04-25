package com.filepilot.vcs.service;

import com.filepilot.vcs.dto.request.CreateDocumentRequest;
import com.filepilot.vcs.dto.response.DocumentResponse;
import com.filepilot.vcs.exception.AccessDeniedException;
import com.filepilot.vcs.exception.ResourceNotFoundException;
import com.filepilot.vcs.mapper.DocumentMapper;
import com.filepilot.vcs.model.*;
import com.filepilot.vcs.repository.CommentRepository;
import com.filepilot.vcs.repository.DocumentRepository;
import com.filepilot.vcs.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final CommentRepository commentRepository;
    private final DocumentMapper mapper;
    private final AuditService auditService;

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(mapper::toDocumentResponse)
                .collect(Collectors.toList());
    }

    public DocumentResponse getDocumentById(Long id) {
        Document document = findDocumentOrThrow(id);
        return mapper.toDocumentResponse(document);
    }

    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request, User author) {
        if (author.getRole() != Role.AUTHOR && author.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only authors and admins can create documents");
        }

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setOwner(author);

        String baseSlug = Document.generateSlug(request.getTitle());
        String slug = baseSlug;
        int suffix = 1;
        while (documentRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix++;
        }
        document.setSlug(slug);

        Document saved = documentRepository.save(document);

        DocumentVersion version = new DocumentVersion();
        version.setDocument(saved);
        version.setVersionNumber(1);
        version.setContent(request.getContent());
        version.setStatus(VersionStatus.DRAFT);
        version.setAuthor(author);

        versionRepository.save(version);

        auditService.log(author, "DOCUMENT_CREATED", "DOCUMENT", saved.getId(),
                "Created document: " + saved.getTitle());

        return mapper.toDocumentResponse(saved);
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, String title, String description, User user) {
        Document document = findDocumentOrThrow(id);

        if (!document.getOwner().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only the owner or admin can update this document");
        }

        document.setTitle(title);
        if (description != null) {
            document.setDescription(description);
        }
        documentRepository.save(document);

        auditService.log(user, "DOCUMENT_UPDATED", "DOCUMENT", id,
                "Updated document: " + title);

        return mapper.toDocumentResponse(document);
    }

    @Transactional
    public void deleteDocument(Long id, User user) {
        Document document = findDocumentOrThrow(id);

        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can delete documents");
        }

        document.setActiveVersion(null);
        documentRepository.save(document);

        // Comments have no DB-level cascade from version_id; delete them before the version cascade fires.
        for (DocumentVersion version : versionRepository.findByDocumentOrderByVersionNumberDesc(document)) {
            commentRepository.deleteByVersion(version);
        }

        documentRepository.delete(document);

        auditService.log(user, "DOCUMENT_DELETED", "DOCUMENT", id,
                "Deleted document: " + document.getTitle());
    }

    public DocumentResponse getDocumentBySlug(String slug) {
        Document document = findDocumentBySlugOrThrow(slug);
        return mapper.toDocumentResponse(document);
    }

    public Document findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    public Document findDocumentBySlugOrThrow(String slug) {
        return documentRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + slug));
    }
}
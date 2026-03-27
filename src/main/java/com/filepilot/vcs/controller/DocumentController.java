package com.filepilot.vcs.controller;

import com.filepilot.vcs.dto.request.CreateDocumentRequest;
import com.filepilot.vcs.dto.response.DocumentResponse;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.security.SecurityUtil;
import com.filepilot.vcs.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<DocumentResponse> getDocumentBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(documentService.getDocumentBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody CreateDocumentRequest request) {
        User user = securityUtil.getCurrentUser();
        DocumentResponse response = documentService.createDocument(request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody CreateDocumentRequest request) {
        User user = securityUtil.getCurrentUser();
        DocumentResponse response = documentService.updateDocument(id, request.getTitle(), request.getDescription(), user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        User user = securityUtil.getCurrentUser();
        documentService.deleteDocument(id, user);
        return ResponseEntity.noContent().build();
    }
}

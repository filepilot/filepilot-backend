package com.filepilot.vcs.controller;

import com.filepilot.vcs.dto.request.CreateVersionRequest;
import com.filepilot.vcs.dto.request.ReviewVersionRequest;
import com.filepilot.vcs.dto.response.VersionResponse;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.security.SecurityUtil;
import com.filepilot.vcs.service.VersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;
    private final SecurityUtil securityUtil;

    @GetMapping("/documents/{documentId}/versions")
    public ResponseEntity<List<VersionResponse>> getVersions(@PathVariable Long documentId) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.getVersionsByDocument(documentId, user));
    }

    @GetMapping("/documents/by-slug/{slug}/versions")
    public ResponseEntity<List<VersionResponse>> getVersionsBySlug(@PathVariable String slug) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.getVersionsByDocumentSlug(slug, user));
    }

    @GetMapping("/documents/by-slug/{slug}/versions/{versionNumber}")
    public ResponseEntity<VersionResponse> getVersionBySlugAndNumber(
            @PathVariable String slug, @PathVariable Integer versionNumber) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.getVersionBySlugAndNumber(slug, versionNumber, user));
    }

    @GetMapping("/versions/pending-review")
    public ResponseEntity<List<VersionResponse>> getPendingReviewVersions() {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.getPendingVersions(user));
    }

    @GetMapping("/versions/{id}")
    public ResponseEntity<VersionResponse> getVersion(@PathVariable Long id) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.getVersionById(id, user));
    }

    @PostMapping("/documents/{documentId}/versions")
    public ResponseEntity<VersionResponse> createVersion(
            @PathVariable Long documentId,
            @Valid @RequestBody CreateVersionRequest request) {
        User user = securityUtil.getCurrentUser();
        VersionResponse response = versionService.createVersion(documentId, request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/versions/{id}/submit")
    public ResponseEntity<VersionResponse> submitForReview(@PathVariable Long id) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.submitForReview(id, user));
    }

    @PutMapping("/versions/{id}/approve")
    public ResponseEntity<VersionResponse> approveVersion(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewVersionRequest request) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.approveVersion(id, request, user));
    }

    @PutMapping("/versions/{id}/reject")
    public ResponseEntity<VersionResponse> rejectVersion(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewVersionRequest request) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(versionService.rejectVersion(id, request, user));
    }

    @DeleteMapping("/versions/{id}")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id) {
        User user = securityUtil.getCurrentUser();
        versionService.deleteVersion(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/documents/{documentId}/versions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VersionResponse> uploadVersion(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (file.getSize() > CreateVersionRequest.MAX_CONTENT_BYTES) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }

        String originalFilename = file.getOriginalFilename();
        String safeFilename = sanitizeFilename(originalFilename);
        if (safeFilename == null || !safeFilename.toLowerCase().endsWith(".txt")) {
            return ResponseEntity.badRequest().build();
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String versionName = (name != null && !name.isBlank()) ? name : safeFilename;
        if (versionName.length() > CreateVersionRequest.MAX_NAME_LENGTH) {
            versionName = versionName.substring(0, CreateVersionRequest.MAX_NAME_LENGTH);
        }

        CreateVersionRequest request = new CreateVersionRequest();
        request.setContent(content);
        request.setName(versionName);

        User user = securityUtil.getCurrentUser();
        VersionResponse response = versionService.createVersion(documentId, request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        // Strip any path components a client may have injected (foo/../bar.txt, C:\evil.txt).
        String base = raw.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        if (base.isBlank() || base.equals(".") || base.equals("..")) {
            return null;
        }
        return base;
    }
}

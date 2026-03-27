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
        return ResponseEntity.ok(versionService.getVersionsByDocument(documentId));
    }

    @GetMapping("/documents/by-slug/{slug}/versions")
    public ResponseEntity<List<VersionResponse>> getVersionsBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(versionService.getVersionsByDocumentSlug(slug));
    }

    @GetMapping("/documents/by-slug/{slug}/versions/{versionNumber}")
    public ResponseEntity<VersionResponse> getVersionBySlugAndNumber(
            @PathVariable String slug, @PathVariable Integer versionNumber) {
        return ResponseEntity.ok(versionService.getVersionBySlugAndNumber(slug, versionNumber));
    }

    @GetMapping("/versions/pending-review")
    public ResponseEntity<List<VersionResponse>> getPendingReviewVersions() {
        return ResponseEntity.ok(versionService.getPendingVersions());
    }

    @GetMapping("/versions/{id}")
    public ResponseEntity<VersionResponse> getVersion(@PathVariable Long id) {
        return ResponseEntity.ok(versionService.getVersionById(id));
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

    @PostMapping(value = "/documents/{documentId}/versions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VersionResponse> uploadVersion(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".txt")) {
            return ResponseEntity.badRequest().build();
        }
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String versionName = (name != null && !name.isBlank()) ? name : originalFilename;

        CreateVersionRequest request = new CreateVersionRequest();
        request.setContent(content);
        request.setName(versionName);

        User user = securityUtil.getCurrentUser();
        VersionResponse response = versionService.createVersion(documentId, request, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

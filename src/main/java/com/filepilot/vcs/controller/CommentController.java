package com.filepilot.vcs.controller;

import com.filepilot.vcs.dto.request.CommentRequest;
import com.filepilot.vcs.dto.response.CommentResponse;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.security.SecurityUtil;
import com.filepilot.vcs.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final SecurityUtil securityUtil;

    @GetMapping("/{versionId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long versionId) {
        User user = securityUtil.getCurrentUser();
        return ResponseEntity.ok(commentService.getCommentsByVersion(versionId, user));
    }

    @PostMapping("/{versionId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long versionId,
            @Valid @RequestBody CommentRequest request) {
        User user = securityUtil.getCurrentUser();
        CommentResponse response = commentService.addComment(versionId, request.getContent(), user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

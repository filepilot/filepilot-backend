package com.filepilot.vcs.controller;

import com.filepilot.vcs.dto.response.AuditLogResponse;
import com.filepilot.vcs.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAllLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    @GetMapping("/audit-logs/{entityType}")
    public ResponseEntity<List<AuditLogResponse>> getLogsByType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditService.getLogsByEntityType(entityType));
    }
}

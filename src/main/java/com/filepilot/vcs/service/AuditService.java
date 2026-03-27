package com.filepilot.vcs.service;

import com.filepilot.vcs.model.AuditLog;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.repository.AuditLogRepository;
import com.filepilot.vcs.dto.response.AuditLogResponse;
import com.filepilot.vcs.mapper.DocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final DocumentMapper mapper;

    public void log(User user, String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(mapper::toAuditLogResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType).stream()
                .map(mapper::toAuditLogResponse)
                .collect(Collectors.toList());
    }
}
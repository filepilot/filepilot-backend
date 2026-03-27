package com.filepilot.vcs.repository;

import com.filepilot.vcs.model.AuditLog;
import com.filepilot.vcs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);

    void deleteByUser(User user);
}
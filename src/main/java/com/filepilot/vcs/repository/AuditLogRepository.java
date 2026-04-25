package com.filepilot.vcs.repository;

import com.filepilot.vcs.model.AuditLog;
import com.filepilot.vcs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);

    // Preserve audit history when a user is deleted: drop the FK, keep the row.
    @Modifying
    @Query("UPDATE AuditLog a SET a.user = NULL WHERE a.user = :user")
    void clearUserReference(@Param("user") User user);
}
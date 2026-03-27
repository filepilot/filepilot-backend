package com.filepilot.vcs.repository;

import com.filepilot.vcs.model.Document;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.User;
import com.filepilot.vcs.model.VersionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentOrderByVersionNumberDesc(Document document);

    Optional<DocumentVersion> findByDocumentAndVersionNumber(Document document, Integer versionNumber);

    Optional<DocumentVersion> findTopByDocumentOrderByVersionNumberDesc(Document document);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.document = :document ORDER BY dv.versionNumber DESC LIMIT 1")
    Optional<DocumentVersion> findTopByDocumentForUpdate(Document document);

    List<DocumentVersion> findByStatus(VersionStatus status);

    @Modifying
    @Query("UPDATE DocumentVersion dv SET dv.reviewer = NULL WHERE dv.reviewer = :user")
    void clearReviewerByUser(User user);

    List<DocumentVersion> findByAuthor(User author);
}
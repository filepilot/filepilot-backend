package com.filepilot.vcs.repository;

import com.filepilot.vcs.model.Document;
import com.filepilot.vcs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwner(User owner);

    List<Document> findByTitleContainingIgnoreCase(String keyword);

    Optional<Document> findBySlug(String slug);
}
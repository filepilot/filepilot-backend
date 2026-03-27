package com.filepilot.vcs.repository;

import com.filepilot.vcs.model.Comment;
import com.filepilot.vcs.model.DocumentVersion;
import com.filepilot.vcs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByVersionOrderByCreatedAtDesc(DocumentVersion version);

    void deleteByAuthor(User author);

    void deleteByVersion(DocumentVersion version);
}
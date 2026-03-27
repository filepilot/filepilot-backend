package com.filepilot.vcs.dto.response;

import com.filepilot.vcs.model.VersionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VersionResponse {

    private Long id;
    private Long documentId;
    private String documentTitle;
    private String documentSlug;
    private Integer versionNumber;
    private String name;
    private String content;
    private VersionStatus status;
    private String authorUsername;
    private String reviewerUsername;
    private String reviewComment;
    private LocalDateTime createdAt;
}
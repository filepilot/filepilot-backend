package com.filepilot.vcs.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentResponse {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private String ownerUsername;
    private Long activeVersionId;
    private Integer activeVersionNumber;
    private Integer totalVersions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
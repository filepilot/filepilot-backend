package com.filepilot.vcs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 5000, message = "Comment must be less than 5000 characters")
    private String content;
}

package com.filepilot.vcs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVersionRequest {

    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_CONTENT_BYTES = 1_048_576;

    @Size(max = MAX_NAME_LENGTH, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Content is required")
    @Size(max = MAX_CONTENT_BYTES, message = "Content must be less than 1 MB")
    private String content;
}
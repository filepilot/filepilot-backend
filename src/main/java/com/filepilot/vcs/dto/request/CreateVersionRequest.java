package com.filepilot.vcs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVersionRequest {

    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Content is required")
    @Size(max = 1_048_576, message = "Content must be less than 1 MB")
    private String content;
}
package com.filepilot.vcs.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVersionRequest {

    private String name;

    @NotBlank(message = "Content is required")
    private String content;
}
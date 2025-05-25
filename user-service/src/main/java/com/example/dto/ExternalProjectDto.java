package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExternalProjectDto (
        String id,

        @NotBlank(message = "Project name must not be blank")
        @Size(max = 120, message = "Project name should be less then 120 characters")
        String name
) {}

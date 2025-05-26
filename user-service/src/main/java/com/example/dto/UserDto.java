package com.example.dto;

import java.util.Set;

public record UserDto(
        Long id,
        String email,
        String name,
        Set<ExternalProjectDto> projects
) {}

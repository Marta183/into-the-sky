package com.example.dto;

public record UserDto(
        Long id,
        String email,
        String name
//        Set<ExternalProjectDto> projects
) {}

package com.example.dto.user;

public record UserDto(
        Long id,
        String email,
        String name
//        Set<ExternalProjectDto> projects
) {}

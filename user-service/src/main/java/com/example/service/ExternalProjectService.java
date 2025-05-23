package com.example.service;

import com.example.dto.user.ExternalProjectDto;

import java.util.Set;

public interface ExternalProjectService {

    ExternalProjectDto addProjectToUser(Long userId, ExternalProjectDto request);

    Set<ExternalProjectDto> findProjectsByUser(Long userId);
}

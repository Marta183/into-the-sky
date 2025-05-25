package com.example.service;

import com.example.dto.ExternalProjectDto;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

public interface ExternalProjectService {

    List<ExternalProjectDto> findAll();
    ExternalProjectDto findById(String id);

    ExternalProjectDto addProjectToUser(Long userId, ExternalProjectDto request);

    Set<ExternalProjectDto> findProjectsByUser(Long userId);

    ExternalProjectDto createProject(ExternalProjectDto request);

    void deleteProject(String id);
}

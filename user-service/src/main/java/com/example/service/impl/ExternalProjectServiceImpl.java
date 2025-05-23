package com.example.service.impl;

import com.example.dto.mappers.ExternalProjectMapper;
import com.example.dto.user.ExternalProjectDto;
import com.example.entity.ExternalProject;
import com.example.entity.User;
import com.example.repository.ExternalProjectRepository;
import com.example.repository.UserRepository;
import com.example.service.ExternalProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalProjectServiceImpl implements ExternalProjectService {

    private final UserRepository userRepository;
    private final ExternalProjectRepository projectRepository;
    private final ExternalProjectMapper projectMapper;

    @Override
    @Transactional(readOnly = true)
    public Set<ExternalProjectDto> findProjectsByUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found" + id));
        return user.getExternalProjects().stream()
                .map(projectMapper::mapToDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public ExternalProjectDto addProjectToUser(Long id, ExternalProjectDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found" + id));
        ExternalProject project = projectRepository.findById(request.id())
                .orElseGet(() -> {
                    ExternalProject newProject = projectMapper.mapToEntity(request);
                    return projectRepository.save(newProject);
                });
        user.getExternalProjects().add(project);
        userRepository.save(user);

        return projectMapper.mapToDto(project);
    }
}

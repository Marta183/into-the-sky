package com.example.service.impl;

import com.example.dto.mappers.ExternalProjectMapper;
import com.example.dto.ExternalProjectDto;
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

import java.util.List;
import java.util.Optional;
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
    public List<ExternalProjectDto> findAll() {
        log.info("Fetching all external projects");
        return projectRepository.findAll().stream()
                .map(projectMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalProjectDto findById(String id) {
        log.info("Finding external project by id {}", id);
        return projectRepository.findById(id)
                .map(projectMapper::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Project not found with id=%s", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<ExternalProjectDto> findProjectsByUser(Long userId) {
        log.info("Fetching projects for user with id {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User not found with id=%d", userId)));
        return user.getExternalProjects().stream()
                .map(projectMapper::mapToDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public ExternalProjectDto createProject(ExternalProjectDto request) {
        log.info("Creating external project: {}", request.name());

        ExternalProject saved = saveProject(request);

        log.info("External project created with id {}", saved.getId());
        return projectMapper.mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteProject(String id) {
        log.info("Attempting to delete external project with id {}", id);

        Optional<ExternalProject> projectOptional = projectRepository.findById(id);
        if (projectOptional.isEmpty()) {
            log.info("Attempt to remove non existing project with id {}", id);
            return;
        }
        ExternalProject project = projectOptional.get();

        assertProjectHasNoUsers(project);

        projectRepository.delete(project);
        log.info("External project deleted with id {}", id);
    }

    @Override
    @Transactional
    public ExternalProjectDto addProjectToUser(Long userId, ExternalProjectDto request) {
        log.info("Binding project {} to user {}", request.id(), userId);

        User user = getUserOrThrow(userId);
        ExternalProject project = getOrCreateProject(request);

        if (user.getExternalProjects().contains(project)) {
            log.info("User {} already linked to project {}", userId, project.getId());
            return projectMapper.mapToDto(project);
        }

        user.getExternalProjects().add(project);
        userRepository.save(user);
        log.info("Project {} successfully added to user {}", project.getId(), userId);

        return projectMapper.mapToDto(project);
    }

    // --- PRIVATE HELPERS ---

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found with id=%d", userId)));
    }

    private ExternalProject getOrCreateProject(ExternalProjectDto dto) {
        if (dto.id() != null) {
            return projectRepository.findById(dto.id())
                    .orElseGet(() -> {
                        log.info("Creating project since it doesn't exist: {}", dto.id());
                        return saveProject(dto);
                    });
        }
        return saveProject(dto);
    }

    private ExternalProject saveProject(ExternalProjectDto dto) {
        return projectRepository.save(projectMapper.mapToEntity(dto));
    }

    private void assertProjectHasNoUsers(ExternalProject project) {
        if (!project.getUsers().isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cannot delete project linked to %d users", project.getUsers().size()));
        }
    }
}

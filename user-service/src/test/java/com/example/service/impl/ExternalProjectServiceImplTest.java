package com.example.service.impl;

import com.example.dto.ExternalProjectDto;
import com.example.dto.mappers.ExternalProjectMapper;
import com.example.entity.ExternalProject;
import com.example.entity.User;
import com.example.repository.ExternalProjectRepository;
import com.example.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalProjectServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ExternalProjectRepository projectRepository;
    @Mock
    private ExternalProjectMapper projectMapper;
    @InjectMocks
    private ExternalProjectServiceImpl service;

    @Test
    void createProject_shouldGenerateUUID() {
        ExternalProjectDto request = new ExternalProjectDto(null, "New Project");
        ExternalProject entity = new ExternalProject("New Project");
        ExternalProject saved = new ExternalProject("New Project");

        when(projectMapper.mapToEntity(request)).thenReturn(entity);
        when(projectRepository.save(any())).thenReturn(saved);
        when(projectMapper.mapToDto(saved)).thenReturn(new ExternalProjectDto("some-id", "New Project"));

        ExternalProjectDto result = service.createProject(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        verify(projectRepository).save(any(ExternalProject.class));
    }

    @Test
    void createProject_shouldFailOnNullName() {
        ExternalProjectDto request = new ExternalProjectDto(null, null);
        ExternalProject entity = new ExternalProject(null);
        when(projectMapper.mapToEntity(request)).thenReturn(entity);

        assertThatThrownBy(() -> service.createProject(request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void findAll_shouldReturnList() {
        List<ExternalProject> entities = List.of(new ExternalProject("A"), new ExternalProject("B"));
        when(projectRepository.findAll()).thenReturn(entities);
        when(projectMapper.mapToDto(any())).thenReturn(new ExternalProjectDto("id", "A"));

        List<ExternalProjectDto> result = service.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findById_shouldReturnProject() {
        ExternalProject entity = new ExternalProject("Test");
        ExternalProjectDto dto = new ExternalProjectDto("123", "Test");
        when(projectRepository.findById("123")).thenReturn(Optional.of(entity));
        when(projectMapper.mapToDto(entity)).thenReturn(dto);

        assertEquals(dto, service.findById("123"));
    }

    @Test
    void findById_shouldThrowIfNotFound() {
        when(projectRepository.findById("xyz")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("xyz"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteProject_shouldSucceedIfNoUsers() {
        ExternalProject entity = new ExternalProject("Clean");
        entity.setUsers(new HashSet<>());
        when(projectRepository.findById("clean")).thenReturn(Optional.of(entity));

        service.deleteProject("clean");

        verify(projectRepository).delete(entity);
    }

    @Test
    void deleteProject_shouldFailIfLinkedToUsers() {
        ExternalProject entity = new ExternalProject("Busy");
        User user = new User();
        entity.setUsers(Set.of(user));
        when(projectRepository.findById("busy")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.deleteProject("busy"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addProjectToUser_shouldAddExistingProject() {
        ExternalProjectDto dto = new ExternalProjectDto("proj-1", "Cool");
        User user = new User();
        user.setExternalProjects(new HashSet<>());
        ExternalProject project = new ExternalProject("Cool");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(projectRepository.findById("proj-1")).thenReturn(Optional.of(project));
        when(projectMapper.mapToDto(project)).thenReturn(dto);

        service.addProjectToUser(10L, dto);

        assertThat(user.getExternalProjects()).contains(project);
    }

    @Test
    void addProjectToUser_shouldCreateAndAddNewProject() {
        ExternalProject project = new ExternalProject("New One");
        ExternalProjectDto dto = new ExternalProjectDto("new-proj", "New One");
        User user = new User();
        user.setExternalProjects(new HashSet<>());

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(projectRepository.findById("new-proj")).thenReturn(Optional.empty());
        when(projectMapper.mapToEntity(dto)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.mapToDto(project)).thenReturn(dto);

        service.addProjectToUser(2L, dto);

        assertThat(user.getExternalProjects()).contains(project);
    }

    @Test
    void addProjectToUser_shouldIgnoreIfAlreadyLinked() {
        ExternalProjectDto dto = new ExternalProjectDto("existing", "Old");
        ExternalProject project = new ExternalProject("Old");
        User user = new User();
        user.setExternalProjects(new HashSet<>(Set.of(project)));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.findById("existing")).thenReturn(Optional.of(project));
        when(projectMapper.mapToDto(project)).thenReturn(dto);

        service.addProjectToUser(1L, dto);

        assertThat(user.getExternalProjects()).hasSize(1);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addProjectToUser_shouldFailOnNullId() {
        ExternalProjectDto dto = new ExternalProjectDto(null, "X");

        assertThatThrownBy(() -> service.addProjectToUser(1L, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addProjectToUser_shouldFailIfUserNotFound() {
        ExternalProjectDto dto = new ExternalProjectDto("123", "X");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addProjectToUser(999L, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

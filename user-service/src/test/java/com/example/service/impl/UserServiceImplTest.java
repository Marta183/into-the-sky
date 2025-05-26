package com.example.service.impl;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.dto.mappers.UserMapper;
import com.example.entity.ExternalProject;
import com.example.entity.User;
import com.example.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_shouldSaveAndReturnDto() {
        UserCreateRequest request = new UserCreateRequest("Test@Email.com", "pass", "name");
        User user = new User();
        user.setEmail("test@email.com");

        when(userMapper.mapToEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToDto(user)).thenReturn(new UserDto(1L, "test@email.com", "name"));

        UserDto result = userService.createUser(request);

        assertThat(result.email()).isEqualTo("test@email.com");
        verify(userRepository).save(argThat(saved -> saved.getPassword().equals("hashed")));
    }

    @Test
    void createUser_shouldFailWhenEmailExists() {
        UserCreateRequest request = new UserCreateRequest("duplicate@example.com", "secret", "name");
        User user = new User();
        user.setEmail("duplicate@example.com");

        when(userMapper.mapToEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void findById_shouldReturnUserDto() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToDto(user)).thenReturn(new UserDto(1L, "user@example.com", "name"));

        UserDto result = userService.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowIfNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_shouldRemoveExisting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        userService.deleteUser(1L);

        verify(userRepository, atMostOnce()).deleteById(1L);
    }

    @Test
    void deleteUser_shouldDoNothingIfUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        userService.deleteUser(999L);

        verify(userRepository, never()).deleteById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldClearProjectsAndDeleteUser() {
        User user = new User();
        user.setId(1L);
        user.getExternalProjects().add(new ExternalProject("1"));
        user.getExternalProjects().add(new ExternalProject("2"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertThat(user.getExternalProjects()).isEmpty();
        verify(userRepository).save(user);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void findAll_shouldReturnMappedUsers() {
        User user = new User(1l, "user@example.com", "passpass", "name", Set.of());

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.mapToDto(user)).thenReturn(new UserDto(1L, "user@example.com", "name"));

        List<UserDto> users = userService.findAll();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).email()).isEqualTo("user@example.com");
    }

    @Test
    void findAll_shouldReturnEmptyListIfNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> users = userService.findAll();

        assertThat(users).isEmpty();
    }
}
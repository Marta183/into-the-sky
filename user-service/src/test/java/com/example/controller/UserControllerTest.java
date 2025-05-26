package com.example.controller;

import com.example.dto.ExternalProjectDto;
import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.exception.GlobalExceptionHandler;
import com.example.security.jwt.JwtProvider;
import com.example.service.ExternalProjectService;
import com.example.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ExternalProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        Mockito.reset(userService, projectService, jwtProvider, authenticationFilter);
    }

    @Test
    void findById_shouldReturnUser() throws Exception {
        UserDto dto = new UserDto(1L, "email@example.com", "ex", Set.of());
        when(userService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("email@example.com"));
    }

    @Test
    void findById_shouldReturn404() throws Exception {
        when(userService.findById(1L)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturnList() throws Exception {
        UserDto user1 = new UserDto(1L, "a@b.com", "a", Set.of());
        UserDto user2 = new UserDto(2L, "c@d.com", "b", Set.of());

        when(userService.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createUser_shouldReturn409WhenEmailExists() throws Exception {
        UserCreateRequest req = new UserCreateRequest("existing@demo.com", "passpass", "name");

        when(userService.createUser(any()))
                .thenThrow(new DataIntegrityViolationException("Email already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_shouldSucceed() throws Exception {
        UserCreateRequest req = new UserCreateRequest("x@y.com", "passpass", "test");
        UserDto res = new UserDto(3L, "x@y.com", "test", Set.of());

        when(userService.createUser(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.email").value("x@y.com"));
    }

    @Test
    void createUser_shouldFailValidation() throws Exception {
        UserCreateRequest invalid = new UserCreateRequest("...", "...", "..."); // invalid email

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/v1/users/42"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(42L);
    }

    @Test
    void deleteUser_shouldReturn204EvenIfUserNotFound() throws Exception {
        doNothing().when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserProjects_shouldReturnSet() throws Exception {
        ExternalProjectDto p1 = new ExternalProjectDto("1", "ABC");
        ExternalProjectDto p2 = new ExternalProjectDto("2", "XYZ");

        when(projectService.findProjectsByUser(99L)).thenReturn(Set.of(p1, p2));

        mockMvc.perform(get("/api/v1/users/99/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void addProjectToUser_shouldSucceed() throws Exception {
        ExternalProjectDto input = new ExternalProjectDto("100", "Test Project");

        when(projectService.addProjectToUser(eq(5L), any())).thenReturn(input);

        mockMvc.perform(post("/api/v1/users/5/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void addProjectToUser_shouldReturn400OnInvalidDto() throws Exception {
        ExternalProjectDto invalid = new ExternalProjectDto(null, "");

        mockMvc.perform(post("/api/v1/users/1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addProjectToUser_shouldReturn404IfUserNotFound() throws Exception {
        ExternalProjectDto dto = new ExternalProjectDto("p1", "Valid");
        when(projectService.addProjectToUser(eq(404L), any()))
                .thenThrow(new EntityNotFoundException("user not found"));

        mockMvc.perform(post("/api/v1/users/404/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProjectToUser_shouldReturn409IfAlreadyLinked() throws Exception {
        ExternalProjectDto dto = new ExternalProjectDto("p1", "Duplicate");

        when(projectService.addProjectToUser(eq(1L), any()))
                .thenThrow(new IllegalStateException("already linked"));

        mockMvc.perform(post("/api/v1/users/1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }
}
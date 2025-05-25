package com.example.controller;

import com.example.dto.ExternalProjectDto;
import com.example.exception.GlobalExceptionHandler;
import com.example.security.jwt.JwtProvider;
import com.example.service.ExternalProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WebMvcTest(ExternalProjectController.class)
class ExternalProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        Mockito.reset(projectService, jwtProvider, authenticationFilter);
    }

    @Test
    void getAllProjects_shouldReturn200AndList() throws Exception {
        ExternalProjectDto dto = new ExternalProjectDto("id-1", "Project A");
        when(projectService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("id-1"));
    }

    @Test
    void createProject_shouldReturn201() throws Exception {
        ExternalProjectDto input = new ExternalProjectDto(null, "Project X");
        ExternalProjectDto saved = new ExternalProjectDto("uuid-123", "Project X");

        when(projectService.createProject(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("uuid-123"));
    }

    @Test
    void createProject_shouldReturn400OnEmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProject_shouldReturn400OnMissingName() throws Exception {
        ExternalProjectDto input = new ExternalProjectDto("id", "");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteProject_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/projects/proj-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProject_shouldReturn409IfLinked() throws Exception {
        Mockito.doThrow(new IllegalStateException("linked")).when(projectService).deleteProject("proj-linked");

        mockMvc.perform(delete("/api/v1/projects/proj-linked"))
                .andExpect(status().isConflict());
    }
}
package com.example.controller;

import com.example.dto.ExternalProjectDto;
import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.service.ExternalProjectService;
import com.example.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ExternalProjectController {

    private final ExternalProjectService projectService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ExternalProjectDto> findAll() {
        log.info("Fetching all projects");
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExternalProjectDto findById(@PathVariable String id) {
        log.info("Fetching project with id {}", id);
        return projectService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExternalProjectDto createProject(@RequestBody @Valid ExternalProjectDto request) {
        log.info("Creating project with name {}", request.name());
        return projectService.createProject(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable String id) {
        log.info("Deleting project with id {}", id);
        projectService.deleteProject(id);
    }
}

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
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final ExternalProjectService projectService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findAll() {
        log.info("Fetching all users");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto findById(@PathVariable Long id) {
        log.info("Fetching user with id {}", id);
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid UserCreateRequest request) {
        log.info("Creating user with email {}", request.email());
        return userService.createUser(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id {}", id);
        userService.deleteUser(id);
    }

    @GetMapping("/{id}/projects")
    @ResponseStatus(HttpStatus.OK)
    public Set<ExternalProjectDto> getProjects(@PathVariable("id") Long userId) {
        log.info("Getting projects for user {}", userId);
        return projectService.findProjectsByUser(userId);
    }

    @PostMapping("/{id}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ExternalProjectDto addProjectToUser(
            @PathVariable("id") Long userId,
            @Valid @RequestBody ExternalProjectDto request
    ) {
        log.info("Adding project {} to user {}", request.id(), userId);
        return projectService.addProjectToUser(userId, request);
    }

}

package com.example.controller;

import com.example.dto.user.ExternalProjectDto;
import com.example.dto.user.UserCreateRequest;
import com.example.dto.user.UserDto;
import com.example.service.ExternalProjectService;
import com.example.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final ExternalProjectService projectService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid UserCreateRequest request) {
        return userService.createUser(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}/projects")
    @ResponseStatus(HttpStatus.OK)
    public Set<ExternalProjectDto> getProjects(
            @PathVariable("id") Long userId) {
        return projectService.findProjectsByUser(userId);
    }

    @PostMapping("/{id}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ExternalProjectDto addProjectToUser(@PathVariable("id") Long userId,
                                         @Valid @RequestBody ExternalProjectDto request) {
        return projectService.addProjectToUser(userId, request);
    }

}

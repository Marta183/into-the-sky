package com.example.controller;

import com.example.dto.auth.AuthenticationRequest;
import com.example.dto.auth.AuthenticationResponse;
import com.example.dto.auth.RefreshTokenRequest;
import com.example.dto.UserCreateRequest;
import com.example.security.auth.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = AuthenticationController.REST_URL)
public class AuthenticationController {
    public static final String REST_URL = "/api/v1/auth";

    private final AuthenticationService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthenticationResponse login(@RequestBody @Valid AuthenticationRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationResponse signup(@RequestBody @Valid UserCreateRequest userRequestDto) {
        return authService.signup(userRequestDto);
    }

    @PostMapping("/refresh")
    public AuthenticationResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refreshAccessToken(request);
    }
}

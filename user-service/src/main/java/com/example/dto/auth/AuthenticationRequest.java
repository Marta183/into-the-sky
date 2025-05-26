package com.example.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password must not be blank")
        String password
) {}

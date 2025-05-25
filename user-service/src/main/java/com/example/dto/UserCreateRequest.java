package com.example.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email should not be empty")
    String email,

    @NotBlank(message = "Password should not be empty")
    @Size(min = 6, max = 20, message = "Password must be at least 6 characters long")
    String password,

    @Size(max = 120, message = "User name is too long")
    String name
) {}

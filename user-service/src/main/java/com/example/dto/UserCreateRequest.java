package com.example.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email should not be empty")
    String email,

    @NotBlank(message = "Password should not be empty")
    @Size(min = 6, max = 20, message = "Password must be at least 6 characters long")
    String password,

    @Size(min = 1, max = 120, message = "User name should be more than 1 and less than 120 characters")
    String name
) {}

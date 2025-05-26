package com.example.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Token must not be blank")
        String refreshToken
) {}

package com.example.dto.auth;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken
) {}

package com.example.dto;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken
) {}

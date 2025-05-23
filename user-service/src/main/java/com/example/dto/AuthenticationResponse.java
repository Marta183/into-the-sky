package com.example.dto;

import com.example.dto.user.UserDto;

public record AuthenticationResponse(
    String accessToken,
    UserDto user
) {}

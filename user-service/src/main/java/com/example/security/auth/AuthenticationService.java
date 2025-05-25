package com.example.security.auth;

import com.example.dto.AuthenticationRequest;
import com.example.dto.AuthenticationResponse;
import com.example.dto.RefreshTokenRequest;
import com.example.dto.UserCreateRequest;

public interface AuthenticationService {
    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse signup(UserCreateRequest userDto);

    AuthenticationResponse refreshAccessToken(RefreshTokenRequest request);
}

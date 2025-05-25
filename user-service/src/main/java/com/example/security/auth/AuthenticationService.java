package com.example.security.auth;

import com.example.dto.auth.AuthenticationRequest;
import com.example.dto.auth.AuthenticationResponse;
import com.example.dto.auth.RefreshTokenRequest;
import com.example.dto.UserCreateRequest;

public interface AuthenticationService {
    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse signup(UserCreateRequest userDto);

    AuthenticationResponse refreshAccessToken(RefreshTokenRequest request);
}

package com.example.security.auth;

import com.example.dto.*;
import com.example.dto.UserDto;
import com.example.dto.auth.AuthenticationRequest;
import com.example.dto.auth.AuthenticationResponse;
import com.example.dto.auth.RefreshTokenRequest;
import com.example.security.jwt.JwtProvider;
import com.example.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional // TODO
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authManager;

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        log.debug("Attempting login for user: {}", request.email());

        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );
            log.info("User {} authenticated successfully", request.email());

            String accessToken = jwtProvider.generateAccessToken(auth.getName());
            String refreshToken = jwtProvider.generateRefreshToken(auth.getName());

            log.debug("Access and refresh tokens generated for user '{}'", request.email());
            return new AuthenticationResponse(accessToken, refreshToken);

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user '{}': {}", request.email(), e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    public AuthenticationResponse signup(UserCreateRequest request) {
        log.debug("Registering new user with email: {}", request.email());

        UserDto userDto = userService.createUser(request);
        log.info("New user registered with email: {}", userDto.email());

        // auto-login after signup
        AuthenticationResponse response = login(
            new AuthenticationRequest(request.email(), request.password())
        );

        log.debug("Auto-login after signup completed for user: {}", request.email());
        return response;
    }

    @Override
    public AuthenticationResponse refreshAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        if (!jwtProvider.isTokenValid(refreshToken)) {
            log.warn("Invalid refresh token attempt");
            throw new CredentialsExpiredException("Refresh token expired or invalid");
        }

        String username;
        try {
            username = jwtProvider.extractUsername(request.refreshToken());
        } catch (JwtException e) {
            log.error("Refresh token extraction failed", e.getMessage() );
            throw new BadCredentialsException("Invalid refresh token");
        }
        String accessToken = jwtProvider.generateAccessToken(username);
        return new AuthenticationResponse(accessToken, refreshToken);
    }
}

package com.example.security.auth;

import com.example.dto.AuthenticationRequest;
import com.example.dto.AuthenticationResponse;
import com.example.dto.RefreshTokenRequest;
import com.example.dto.user.UserCreateRequest;
import com.example.dto.user.UserDto;
import com.example.security.UserInfoDetailsService;
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
//        log.debug("Login for user {}", authRequest.email());

        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );
            log.info("User {} logged in successfully", request.email());

            String accessToken = jwtProvider.generateAccessToken(auth.getName());
            String refreshToken = jwtProvider.generateRefreshToken(auth.getName());
            return new AuthenticationResponse(accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    public AuthenticationResponse signup(UserCreateRequest request) {
//        log.debug("{} registration by email {}", ENTITY_CLASS_NAME, request.email());
        UserDto userDto = userService.createUser(request);

        // auto-login after signup
        return login(
            new AuthenticationRequest(request.email(), request.password())
        );
//        log.debug("New user saved in DB with email {}", userDto.email());
    }

    @Override
    public AuthenticationResponse refreshAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        if (!jwtProvider.isTokenValid(refreshToken)) {
            throw new CredentialsExpiredException("Refresh token expired");
        }
        try {
            jwtProvider.extractAllClaims(refreshToken);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        String username = jwtProvider.extractUsername(request.refreshToken());
        String accessToken = jwtProvider.generateAccessToken(username);
        return new AuthenticationResponse(accessToken, request.refreshToken());
    }
}

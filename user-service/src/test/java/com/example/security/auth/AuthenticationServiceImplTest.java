package com.example.security.auth;

import com.example.dto.UserCreateRequest;
import com.example.dto.UserDto;
import com.example.dto.auth.AuthenticationRequest;
import com.example.dto.auth.AuthenticationResponse;
import com.example.dto.auth.RefreshTokenRequest;
import com.example.security.jwt.JwtProvider;
import com.example.service.UserService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authManager;
    @Mock
    private JwtProvider jwtProvider;
    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void login_shouldReturnTokens_whenValidCredentials() {
        AuthenticationRequest request = new AuthenticationRequest("email", "pass");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("email");
        when(authManager.authenticate(any())).thenReturn(auth);

        when(jwtProvider.generateAccessToken("email")).thenReturn("access");
        when(jwtProvider.generateRefreshToken("email")).thenReturn("refresh");

        AuthenticationResponse response = service.login(request);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void login_shouldThrow_onBadCredentials() {
        AuthenticationRequest request = new AuthenticationRequest("email", "wrong");
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void signup_shouldReturnTokens_onSuccess() {
        UserCreateRequest request = new UserCreateRequest("x", "email", "pass");
        UserDto created = new UserDto(1L, "x", "email", Set.of());

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("email");
        when(authManager.authenticate(any())).thenReturn(auth);

        when(userService.createUser(request)).thenReturn(created);
        when(jwtProvider.generateAccessToken("email")).thenReturn("access");
        when(jwtProvider.generateRefreshToken("email")).thenReturn("refresh");

        AuthenticationResponse response = service.signup(request);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void refresh_shouldReturnNewAccessToken_whenValidRefresh() {
        String refresh = "refreshToken";

        when(jwtProvider.isTokenValid(refresh)).thenReturn(true);
        when(jwtProvider.extractTokenType(any())).thenReturn("REFRESH");
        when(jwtProvider.extractUsername(refresh)).thenReturn("user@mail.com");
        when(jwtProvider.generateAccessToken("user@mail.com")).thenReturn("newAccess");

        AuthenticationResponse response = service.refreshAccessToken(new RefreshTokenRequest(refresh));

        assertThat(response.accessToken()).isEqualTo("newAccess");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
    }

    @Test
    void refresh_shouldThrow_whenRefreshIsInvalid() {
        when(jwtProvider.isTokenValid("bad")).thenReturn(false);

        assertThatThrownBy(() -> service.refreshAccessToken(new RefreshTokenRequest("bad")))
                .isInstanceOf(CredentialsExpiredException.class);
    }

    @Test
    void refresh_shouldThrow_whenTokenIsNotRefreshType() {
        when(jwtProvider.isTokenValid("token")).thenReturn(true);
        when(jwtProvider.extractTokenType(any())).thenReturn("ACCESS");

        assertThatThrownBy(() -> service.refreshAccessToken(new RefreshTokenRequest("token")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid token type");
    }

    @Test
    void refresh_shouldThrow_whenClaimExtractionFails() {
        when(jwtProvider.isTokenValid("token")).thenReturn(true);
        when(jwtProvider.extractTokenType(any())).thenThrow(new JwtException("error"));

        assertThatThrownBy(() -> service.refreshAccessToken(new RefreshTokenRequest("token")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid refresh token");
    }
}
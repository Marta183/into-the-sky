package com.example.controller;

import com.example.dto.UserCreateRequest;
import com.example.dto.auth.AuthenticationRequest;
import com.example.dto.auth.AuthenticationResponse;
import com.example.dto.auth.RefreshTokenRequest;
import com.example.exception.GlobalExceptionHandler;
import com.example.security.auth.AuthenticationService;
import com.example.security.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authService;

    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        Mockito.reset(authService, jwtProvider, authenticationFilter);
    }

    @Test
    void login_shouldReturn200AndTokens() throws Exception {
        AuthenticationRequest req = new AuthenticationRequest("email@cc.com", "passpass");
        AuthenticationResponse res = new AuthenticationResponse("access", "refresh");

        when(authService.login(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access"))
            .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void login_shouldReturn401_onBadCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthenticationRequest("email@ba.com", "wrongwrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_shouldReturn201AndTokens() throws Exception {
        UserCreateRequest req = new UserCreateRequest("email@cc.com", "passpass", "name");
        AuthenticationResponse res = new AuthenticationResponse("access", "refresh");

        when(authService.signup(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void register_shouldReturn409_onDuplicate() throws Exception {
        when(authService.signup(any())).thenThrow(new EntityExistsException("duplicate"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UserCreateRequest("x@mail.com", "passpass", "p")
                )))
            .andExpect(status().isConflict());
    }

    @Test
    void register_shouldReturn400_onValidationError() throws Exception {
        UserCreateRequest invalid = new UserCreateRequest("", "", "");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_shouldReturn200_withNewAccessToken() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest("refresh");
        AuthenticationResponse res = new AuthenticationResponse("newAccess", "refresh");

        when(authService.refreshAccessToken(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"));
    }

    @Test
    void refresh_shouldReturn401_onExpiredToken() throws Exception {
        when(authService.refreshAccessToken(any()))
                .thenThrow(new CredentialsExpiredException("expired"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("bad"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturn400_onAccessTokenInsteadOfRefresh() throws Exception {
        when(authService.refreshAccessToken(any()))
                .thenThrow(new IllegalArgumentException("not refresh"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("accessToken"))))
                .andExpect(status().isBadRequest());
    }
}
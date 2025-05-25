package com.example.security.jwt;

import io.jsonwebtoken.Claims;

public interface JwtProvider {
    String extractUsername(String token);
    Claims extractAllClaims(String token);

    String generateAccessToken(String username);
    String generateRefreshToken(String username);

    boolean isTokenValid(String token);
}

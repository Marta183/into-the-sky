package com.example.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProviderImpl implements JwtProvider {
    @Value("${spring.security.jwt.signing-key}")
    private String secretKeyString;
    private SecretKey secretKey;

    @Value("${spring.security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${spring.security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${spring.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(String username) {
        return buildJwtToken(new HashMap<>(), username, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(String username) {
        return buildJwtToken(new HashMap<>(), username, refreshTokenExpiration);
    }

    private String buildJwtToken(Map<String, Object> extraClaims, String username, long tokenExpiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuer(getJwtIssuer())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            final String issuer = extractIssuer(token);
            return issuer.equals(getJwtIssuer()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    @Override
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Failed to parse JWT: {}", e.getMessage());
            throw e;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Key getSigningKey() {
        return this.secretKey;
    }

    private String getJwtIssuer() {
        return this.jwtIssuer;
    }
}

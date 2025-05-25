package com.example.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtProviderImpl implements JwtProvider {
    @Value("${spring.security.jwt.signing-key}")
    private String jwtSigningKey;

    @Value("${spring.security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${spring.security.jwt.access-token.expiration}")
    private long accessTokenExpiration;
    @Value("${spring.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

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
        if (StringUtils.isBlank(token))
            return false;

        final String issuer = extractIssuer(token);
        return issuer.equals(getJwtIssuer())
                && !isTokenExpired(token);
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
        return Jwts.parser()
                .setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getJwtIssuer() {
        return this.jwtIssuer;
    }
}

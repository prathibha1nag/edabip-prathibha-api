package com.edabip.billing.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        if (expirationMs <= 0) throw new IllegalArgumentException("JWT_EXPIRATION_MS must be positive");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String createToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder().subject(username).claim("role", role).issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey).compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, String expectedUsername) {
        return expectedUsername != null && expectedUsername.equals(extractUsername(token));
    }

    public String extractRole(String token) {
        return claims(token).get("role", String.class);
    }

    public long getExpirationSeconds() { return expirationMs / 1000; }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload();
    }
}

package com.demo.auth.infrastructure.adapter.out;

import com.demo.auth.application.port.out.TokenPort;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class JwtTokenAdapter implements TokenPort {

    private static final String PREFIX_ACCESS  = "access_token:";
    private static final String PREFIX_REFRESH = "refresh_token:";

    private final Key                 key;
    private final long                accessTtlMs;
    private final long                refreshTtlMs;
    private final StringRedisTemplate redis;

    public JwtTokenAdapter(String secret, long accessTtlMs, long refreshTtlMs,
                           StringRedisTemplate redis) {
        this.key          = Keys.hmacShaKeyFor(Objects.requireNonNull(secret, "secret must be provided")
            .getBytes(StandardCharsets.UTF_8));
        this.accessTtlMs  = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
        this.redis        = Objects.requireNonNull(redis, "redis must be provided");
    }

    @Override
    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTtlMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTtlMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public void storeTokens(String username, String accessToken, String refreshToken) {
        String user = Objects.requireNonNull(username, "username must be provided");
        String access = Objects.requireNonNull(accessToken, "accessToken must be provided");
        String refresh = Objects.requireNonNull(refreshToken, "refreshToken must be provided");
        redis.opsForValue().set(PREFIX_ACCESS  + user, access,  accessTtlMs,  TimeUnit.MILLISECONDS);
        redis.opsForValue().set(PREFIX_REFRESH + user, refresh, refreshTtlMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void revokeTokens(String username) {
        String user = Objects.requireNonNull(username, "username must be provided");
        redis.delete(PREFIX_ACCESS  + user);
        redis.delete(PREFIX_REFRESH + user);
    }

    @Override
    public boolean isAccessTokenActive(String username, String token) {
        String stored = redis.opsForValue().get(PREFIX_ACCESS + username);
        return token.equals(stored);
    }

    @Override
    public boolean isRefreshTokenActive(String username, String token) {
        String stored = redis.opsForValue().get(PREFIX_REFRESH + username);
        return token.equals(stored);
    }

    @Override
    public boolean validateAccessToken(String token) {
        return validateToken(token, "access");
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return validateToken(token, "refresh");
    }

    @Override
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    // ── privados ──────────────────────────────────────────────────────────────

    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return expectedType.equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

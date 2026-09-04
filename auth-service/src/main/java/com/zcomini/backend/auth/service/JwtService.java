package com.zcomini.backend.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.zcomini.backend.auth.config.AuthProperties;
import com.zcomini.backend.auth.entity.RefreshTokenEntity;
import com.zcomini.backend.auth.entity.UserEntity;
import com.zcomini.backend.auth.repository.RefreshTokenRepository;
import com.zcomini.backend.shared.util.HashUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;
    private final SecretKey secretKey;

    public JwtService(AuthProperties authProperties, RefreshTokenRepository refreshTokenRepository) {
        this.authProperties = authProperties;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createAccessToken(UserEntity user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(authProperties.jwtIssuer())
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(authProperties.accessTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String saveRefresh(UserEntity user) {
        String token = UUID.randomUUID().toString();
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(HashUtils.sha256Hex(token));
        entity.setExpiresAt(OffsetDateTime.now().plusDays(authProperties.refreshTokenDays()));
        refreshTokenRepository.save(entity);
        return token;
    }
}

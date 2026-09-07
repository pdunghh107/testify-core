package com.zcomini.backend.auth.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zcomini.backend.auth.entity.RefreshTokenEntity;
import com.zcomini.backend.auth.exception.TokenException;
import com.zcomini.backend.auth.repository.RefreshTokenRepository;
import com.zcomini.backend.auth.service.JwtService;
import com.zcomini.backend.shared.util.HashUtils;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public final class TokenUtils {

    private final JwtService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenEntity findByTokenHash(String tokenValue) {
        return refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(tokenValue))
                .orElseThrow(TokenException::refreshTokenInvalid);
    }

    public void revokeAccessToken(String accessToken) {
        try {
            Claims claims = jwtService.parse(accessToken);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            if (StringUtils.hasText(jti) && expiration != null) {
                long ttlMillis = expiration.getTime() - System.currentTimeMillis();
                if (ttlMillis > 0) {
                    stringRedisTemplate.opsForValue().set(
                            AuthString.TOKEN_REVOKED + jti,
                            "revoked",
                            ttlMillis,
                            TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception ex) {
            log.error("[TOKEN REVOKED] Thu hồi Access Token thất bại: ", ex);
        }
    }

    public void revokeUserAccessToken(UUID userId) {
        stringRedisTemplate.opsForValue().set(
                AuthString.USER_REVOKED + userId.toString(),
                String.valueOf(Instant.now().toEpochMilli()), 30,
                TimeUnit.MINUTES);
    }

    public void revokeRefreshTokenWhenLogout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(refreshToken))
                .ifPresent(token -> {
                    token.setRevokedAt(OffsetDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

}

package com.zcomini.backend.auth.validate;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.zcomini.backend.auth.entity.RefreshTokenEntity;
import com.zcomini.backend.auth.exception.TokenException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class TokenValidator {

    public void checkRefreshTokenValid(RefreshTokenEntity token) {
        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw TokenException.refreshTokenInvalid();
        }
    }
}

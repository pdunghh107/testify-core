package com.zcomini.backend.auth.mapper;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.zcomini.backend.auth.entity.RefreshTokenEntity;

@Component
public final class TokenMapper {

    public void toRevokeTokens(List<RefreshTokenEntity> tokens) {
        tokens.forEach(token -> token.setRevokedAt(OffsetDateTime.now()));
    }
}

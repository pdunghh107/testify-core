package com.zcomini.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.zcomini.backend.auth.entity.UserEntity;

public record RegisterResponse(
        String accessToken,
        @JsonIgnore String refreshToken,
        UserResponse user) {
    public static RegisterResponse from(String accessToken, String refreshToken, UserEntity entity) {
        return new RegisterResponse(accessToken, refreshToken, UserResponse.from(entity));
    }
}

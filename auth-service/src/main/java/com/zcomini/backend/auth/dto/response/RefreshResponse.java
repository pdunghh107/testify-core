package com.zcomini.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RefreshResponse(
        String accessToken,
        @JsonIgnore String refreshToken) {
    public static RefreshResponse from(String accessToken, String refreshToken) {
        return new RefreshResponse(accessToken, refreshToken);
    }
}

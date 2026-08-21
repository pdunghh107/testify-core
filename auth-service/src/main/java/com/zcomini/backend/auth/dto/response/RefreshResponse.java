package com.zcomini.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO đại diện cho kết quả trả về sau khi gia hạn (Refresh) Token thành công.
 *
 * @param accessToken  Chuỗi JWT Access Token mới.
 * @param refreshToken Chuỗi Refresh Token mới (ẩn khỏi body response vì đã được gán vào Cookie).
 */
public record RefreshResponse(
        String accessToken,
        @JsonIgnore String refreshToken) {
    public static RefreshResponse from(String accessToken, String refreshToken) {
        return new RefreshResponse(accessToken, refreshToken);
    }
}

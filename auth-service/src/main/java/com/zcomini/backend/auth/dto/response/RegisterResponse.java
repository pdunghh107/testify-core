package com.zcomini.backend.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zcomini.backend.auth.entity.UserEntity;

/**
 * DTO đại diện cho kết quả trả về sau khi đăng ký tài khoản thành công.
 *
 * @param accessToken  Chuỗi JWT Access Token để đăng nhập ngay lập tức.
 * @param refreshToken Chuỗi Refresh Token (ẩn khỏi body response vì đã được gán vào Cookie).
 * @param user         Thông tin cơ bản của người dùng vừa tạo.
 */
public record RegisterResponse(
        String accessToken,
        @JsonIgnore String refreshToken,
        UserResponse user) {
    public static RegisterResponse from(String accessToken, String refreshToken, UserEntity entity) {
        return new RegisterResponse(accessToken, refreshToken, UserResponse.from(entity));
    }
}

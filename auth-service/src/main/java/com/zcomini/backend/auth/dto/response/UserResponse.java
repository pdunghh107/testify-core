package com.zcomini.backend.auth.dto.response;

import java.util.UUID;

import com.zcomini.backend.auth.entity.UserEntity;

/**
 * DTO đại diện cho thông tin hồ sơ (Profile) của người dùng trả về cho Frontend.
 *
 * @param id        Định danh duy nhất của người dùng.
 * @param email     Địa chỉ email tài khoản.
 * @param fullName  Họ và tên hiển thị.
 * @param phone     Số điện thoại liên hệ.
 * @param avatarUrl Đường dẫn ảnh đại diện.
 * @param role      Vai trò trong hệ thống (vd: user, admin).
 * @param active    Trạng thái kích hoạt của tài khoản.
 */
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        String avatarUrl,
        String role,
        boolean active) {
    public static UserResponse from(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getPhone(),
                entity.getAvatarUrl(),
                entity.getRole(),
                entity.isActive());
    }

}

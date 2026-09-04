package com.zcomini.backend.auth.dto.response;

import java.util.UUID;

import com.zcomini.backend.auth.entity.UserEntity;

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

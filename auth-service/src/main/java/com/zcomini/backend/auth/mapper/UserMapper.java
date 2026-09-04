package com.zcomini.backend.auth.mapper;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.zcomini.backend.auth.dto.request.RegisterRequest;
import com.zcomini.backend.auth.entity.UserEntity;

@Component
public final class UserMapper {

    public UserEntity toRegister(RegisterRequest request, String encodedPassword, String roleCode) {
        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPasswordHash(encodedPassword);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setAvatarUrl(request.avatarUrl());
        user.setRole(roleCode);
        user.setActive(true);
        user.setLastLoginAt(OffsetDateTime.now());
        return user;
    }

}

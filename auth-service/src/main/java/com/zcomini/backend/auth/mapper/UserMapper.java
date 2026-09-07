package com.zcomini.backend.auth.mapper;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zcomini.backend.auth.dto.request.DeactivateRequest;
import com.zcomini.backend.auth.dto.request.RegisterRequest;
import com.zcomini.backend.auth.dto.request.UserRequest;
import com.zcomini.backend.auth.entity.UserEntity;
import com.zcomini.backend.auth.utils.AuthString;

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

    public void toUpdateFieldNeeded(UserEntity user, UserRequest request) {
        if (StringUtils.hasText(request.fullName())) {
            user.setFullName(request.fullName().trim());
        }
        if (StringUtils.hasText(request.phone())) {
            user.setPhone(request.phone().trim());
        }
        if (StringUtils.hasText(request.avatarUrl())) {
            user.setAvatarUrl(request.avatarUrl());
        }
    }

    public void toDeactive(UserEntity user, DeactivateRequest request) {
        user.setActive(false);
        user.setDeletedAt(OffsetDateTime.now());
        user.setDeletionReason(StringUtils.hasText(request.deletedReason()) ? request.deletedReason().trim()
                : AuthString.DELETED_REASON);
    }

}

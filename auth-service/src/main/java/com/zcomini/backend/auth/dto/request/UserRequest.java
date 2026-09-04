package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPhone;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
                @NotBlank(message = "Vui lòng nhập họ và tên") String fullName,
                @ValidPhone String phone,
                String avatarUrl) {
}

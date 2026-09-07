package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPhone;
import com.zcomini.backend.auth.validate.AuthValidateString;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = AuthValidateString.FULL_NAME_REQUIRED) String fullName,
        @ValidPhone String phone,
        String avatarUrl) {
}

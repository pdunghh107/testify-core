package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;
import com.zcomini.backend.auth.validate.AuthValidateString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = AuthValidateString.OLD_PASSWORD_REQUIRED) @Size(max = 72, message = AuthValidateString.PASSWORD_INVALID) String oldPassword,
        @NotBlank(message = AuthValidateString.NEW_PASSWORD_REQUIRED) @ValidPassword String newPassword) {
}

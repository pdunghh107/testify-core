package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;
import com.zcomini.backend.shared.validation.annotation.ValidPhone;
import com.zcomini.backend.auth.validate.AuthValidateString;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
                @NotBlank(message = AuthValidateString.FULL_NAME_REQUIRED) @Size(min = 2, max = 255, message = AuthValidateString.FULL_NAME_INVALID) String fullName,
                @NotBlank(message = AuthValidateString.PHONE_REQUIRED) @ValidPhone String phone,
                @NotBlank(message = AuthValidateString.EMAIL_REQUIRED) @Email(message = AuthValidateString.EMAIL_INVALID) String email,
                @NotBlank(message = AuthValidateString.PASSWORD_REQUIRED) @ValidPassword String password,
                @NotBlank(message = AuthValidateString.CONFIRM_PASSWORD_REQUIRED) String confirmPassword,
                String avatarUrl) {
}

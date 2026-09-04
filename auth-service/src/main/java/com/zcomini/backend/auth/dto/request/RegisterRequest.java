package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;
import com.zcomini.backend.shared.validation.annotation.ValidPhone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
                @NotBlank(message = "{fullname.required}") @Size(min = 2, max = 255, message = "{fullname.invalid}") String fullName,
                @NotBlank(message = "{phone.required}") @ValidPhone String phone,
                @NotBlank(message = "{email.required}") @Email(message = "{email.invalid}") String email,
                @NotBlank(message = "{password.required}") @ValidPassword String password,
                @NotBlank(message = "{password.required}") String confirmPassword,
                String avatarUrl) {
}

package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.auth.validate.AuthValidateString;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = AuthValidateString.EMAIL_REQUIRED) @Email(message = AuthValidateString.EMAIL_INVALID) String email,
        @NotBlank(message = AuthValidateString.PASSWORD_REQUIRED) @Size(max = 72, message = AuthValidateString.PASSWORD_INVALID) String password) {
}

package com.zcomini.backend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "{email.required}") @Email(message = "{email.invalid}") String email,
        @NotBlank(message = "{password.required}") @Size(max = 100) String password) {
}

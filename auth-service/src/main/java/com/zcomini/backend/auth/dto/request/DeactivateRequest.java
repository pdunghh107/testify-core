package com.zcomini.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeactivateRequest(
        @NotBlank(message = "{password.required}") String password) {
}

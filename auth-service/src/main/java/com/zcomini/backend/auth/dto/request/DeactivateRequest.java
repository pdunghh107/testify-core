package com.zcomini.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeactivateRequest(
        @NotBlank(message = "Vui lòng nhập mật khẩu") @Size(max = 72, message = "Mật khẩu không hợp lệ") String password) {
}

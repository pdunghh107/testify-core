package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
                @NotBlank(message = "Vui lòng nhập mật khẩu cũ") @Size(max = 72, message = "Mật khẩu không hợp lệ") String oldPassword,
                @NotBlank(message = "Vui lòng nhập mật khẩu mới") @ValidPassword String newPassword) {
}

package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;
import com.zcomini.backend.shared.validation.annotation.ValidPhone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Vui lòng nhập họ và tên") @Size(min = 2, max = 255, message = "Họ và tên không hợp lệ") String fullName,
        @NotBlank(message = "Vui lòng nhập số điện thoại") @ValidPhone String phone,
        @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không hợp lệ") String email,
        @NotBlank(message = "Vui lòng nhập mật khẩu") @ValidPassword String password,
        @NotBlank(message = "Vui lòng nhập lại mật khẩu") String confirmPassword,
        String avatarUrl) {
}

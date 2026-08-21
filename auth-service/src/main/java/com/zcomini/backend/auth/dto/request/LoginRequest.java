package com.zcomini.backend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO đại diện cho yêu cầu đăng nhập hệ thống.
 *
 * @param email    Địa chỉ email đăng nhập, phải đúng định dạng email.
 * @param password Mật khẩu người dùng nhập vào.
 */
public record LoginRequest(
                @NotBlank(message = "{email.required}") @Email(message = "{email.invalid}") String email,
                @NotBlank(message = "{password.required}") @Size(max = 100) String password) {
}

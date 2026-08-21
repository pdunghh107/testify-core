package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;
import com.zcomini.backend.shared.validation.annotation.ValidPhone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO đại diện cho yêu cầu đăng ký tài khoản mới.
 *
 * @param fullName        Họ và tên đầy đủ của người dùng.
 * @param phone           Số điện thoại liên lạc, phải đúng định dạng chuẩn.
 * @param email           Địa chỉ email, sử dụng làm tài khoản đăng nhập.
 * @param password        Mật khẩu bảo mật cho tài khoản mới.
 * @param confirmPassword Mật khẩu xác nhận, phải khớp với mật khẩu.
 * @param avatarUrl       Đường dẫn ảnh đại diện (không bắt buộc).
 */
public record RegisterRequest(
        @NotBlank(message = "{fullname.required}") @Size(min = 2, max = 255, message = "{fullname.invalid}") String fullName,
        @NotBlank(message = "{phone.required}") @ValidPhone String phone,
        @NotBlank(message = "{email.required}") @Email(message = "{email.invalid}") String email,
        @NotBlank(message = "{password.required}") @ValidPassword String password,
        @NotBlank(message = "{password.required}") String confirmPassword,
        String avatarUrl) {
}

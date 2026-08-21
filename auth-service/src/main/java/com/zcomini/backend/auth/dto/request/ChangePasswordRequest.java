package com.zcomini.backend.auth.dto.request;

import com.zcomini.backend.shared.validation.annotation.ValidPassword;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO đại diện cho yêu cầu thay đổi mật khẩu của người dùng hiện tại.
 * <p>
 * Hệ thống sẽ kiểm tra {@code oldPassword} có khớp với mật khẩu đang lưu trữ hay không
 * trước khi cho phép cập nhật sang {@code newPassword}.
 *
 * @param oldPassword Mật khẩu hiện tại của người dùng, không được để trống (bắt buộc nhập).
 * @param newPassword Mật khẩu mới, bắt buộc phải thỏa mãn các tiêu chí bảo mật mạnh
 *                    (bao gồm chiều dài tối thiểu, có chứa ký tự đặc biệt, số, chữ hoa và thường).
 */
public record ChangePasswordRequest(
                @NotBlank(message = "{password.required}") String oldPassword,
                @ValidPassword String newPassword) {
}

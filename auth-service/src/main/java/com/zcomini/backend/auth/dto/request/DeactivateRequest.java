package com.zcomini.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO đại diện cho yêu cầu vô hiệu hoá (khóa) tài khoản người dùng.
 * <p>
 * Để đảm bảo an toàn và tránh việc vô ý khóa tài khoản, người dùng buộc phải 
 * xác nhận lại bằng mật khẩu hiện tại.
 *
 * @param password Mật khẩu hiện tại dùng để xác thực quyền vô hiệu hoá, không được để trống.
 */
public record DeactivateRequest(
                @NotBlank(message = "{password.required}") String password) {
}

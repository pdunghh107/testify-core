package com.zcomini.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.zcomini.backend.shared.validation.annotation.ValidPhone;
/**
 * DTO đại diện cho yêu cầu cập nhật hồ sơ cá nhân của người dùng.
 * <p>
 * Các thông tin được gửi lên sẽ ghi đè lên thông tin hiện tại của người dùng.
 * Thuộc tính {@code fullName} là bắt buộc, trong khi {@code phone} và {@code avatarUrl}
 * là các trường tùy chọn.
 *
 * @param fullName  Họ và tên hiển thị mới của người dùng, không được để trống.
 * @param phone     Số điện thoại liên hệ mới, phải tuân thủ đúng định dạng số điện thoại hợp lệ (có thể {@code null}).
 * @param avatarUrl Đường dẫn (URL) trỏ đến ảnh đại diện mới (có thể {@code null}).
 */
public record UserRequest(
                @NotBlank(message = "{fullname.required}") String fullName,
                @ValidPhone String phone,
                String avatarUrl) {
}

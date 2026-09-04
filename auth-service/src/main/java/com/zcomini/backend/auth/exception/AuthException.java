package com.zcomini.backend.auth.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class AuthException {

    private AuthException() {
    }

    public static BusinessException tokenExpired() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Phiên đăng nhập đã hết hạn.");
    }

    public static BusinessException tokenInvalid() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Phiên đăng nhập không hợp lệ.");
    }

    public static BusinessException tokenRevoked() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Phiên đăng nhập đã bị thu hồi.");
    }

    public static BusinessException refreshTokenInvalid() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.");
    }

    public static BusinessException credentialsInvalid() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Email hoặc mật khẩu không chính xác.");
    }

    public static BusinessException userNotFound() {
        return new BusinessException(HttpStatus.NOT_FOUND,
                ApiErrorCode.NOT_FOUND.value(),
                "Người dùng không tồn tại");
    }

    public static BusinessException userInactive() {
        return new BusinessException(HttpStatus.LOCKED,
                ApiErrorCode.LOCKED.value(),
                "Tài khoản đã bị vô hiệu hóa. Vui lòng liên hệ hỗ trợ.");
    }

    public static BusinessException userLocked() {
        return new BusinessException(HttpStatus.TOO_MANY_REQUESTS,
                ApiErrorCode.TOO_MANY_REQUESTS.value(),
                "Tài khoản tạm thời bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau");
    }

    public static BusinessException emailTaken() {
        return new BusinessException(HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT.value(),
                "Email đã được sử dụng");
    }

    public static BusinessException phoneTaken() {
        return new BusinessException(HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT.value(),
                "Số điện thoại đã được sử dụng");
    }

    public static BusinessException passwordMismatch() {
        return new BusinessException(HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST.value(),
                "Mật khẩu xác nhận không khớp.");
    }

    public static BusinessException newPasswordMustBeDifferent() {
        return new BusinessException(HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST.value(),
                "Mật khẩu mới không được trùng với mật khẩu cũ.");
    }
}

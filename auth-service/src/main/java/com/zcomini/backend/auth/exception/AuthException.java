package com.zcomini.backend.auth.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class AuthException {

    private AuthException() {
    }

    public static BusinessException credentialsInvalid() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Email hoặc mật khẩu không chính xác.");
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

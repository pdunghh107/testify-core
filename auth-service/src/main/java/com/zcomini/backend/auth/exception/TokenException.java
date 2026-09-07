package com.zcomini.backend.auth.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class TokenException {

    private TokenException() {
    }

    public static BusinessException tokenInvalid() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Phiên đăng nhập không hợp lệ.");
    }

    public static BusinessException tokenExpired() {
        return new BusinessException(HttpStatus.UNAUTHORIZED,
                ApiErrorCode.UNAUTHORIZED.value(),
                "Phiên đăng nhập đã hết hạn.");
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
}

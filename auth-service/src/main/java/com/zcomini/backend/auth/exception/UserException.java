package com.zcomini.backend.auth.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class UserException {

    private UserException() {
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
}
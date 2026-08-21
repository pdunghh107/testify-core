package com.zcomini.backend.testify.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class RequestException {
    private RequestException() {
    }

    public static BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND,
                ApiErrorCode.NOT_FOUND.value(),
                "Cấu hình API không tồn tại.");
    }
}

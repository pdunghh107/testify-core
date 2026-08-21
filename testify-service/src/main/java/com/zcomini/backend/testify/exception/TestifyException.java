package com.zcomini.backend.testify.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class TestifyException {

    private TestifyException() {
    }

    public static BusinessException accessDenied() {
        return new BusinessException(HttpStatus.FORBIDDEN,
                ApiErrorCode.FORBIDDEN.value(),
                "Bạn không có quyền thao tác trên tài nguyên này.");
    }

}

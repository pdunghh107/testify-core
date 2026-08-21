package com.zcomini.backend.testify.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class WorkspaceException {
    private WorkspaceException() {
    }

    public static BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND,
                ApiErrorCode.NOT_FOUND.value(),
                "Dự án không tồn tại.");
    }

    public static BusinessException hasData() {
        return new BusinessException(HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT.value(),
                "Không thể xóa dự án đang chứa dữ liệu.");
    }
}

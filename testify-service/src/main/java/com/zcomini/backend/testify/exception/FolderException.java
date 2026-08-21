package com.zcomini.backend.testify.exception;

import org.springframework.http.HttpStatus;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;

public final class FolderException {

    private FolderException() {
    }

    public static BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND,
                ApiErrorCode.NOT_FOUND.value(),
                "Thư mục không tồn tại.");
    }

    public static BusinessException hasData() {
        return new BusinessException(HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT.value(),
                "Không thể xóa thư mục đang chứa dữ liệu.");
    }

    public static BusinessException invalidParentFolder() {
        return new BusinessException(HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST.value(),
                "Thư mục cha không hợp lệ hoặc không thuộc dự án này.");
    }

    public static BusinessException maxDepthExceeded() {
        return new BusinessException(HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST.value(),
                "Vượt quá độ sâu thư mục tối đa (Độ sâu tối đa là 3).");
    }

}

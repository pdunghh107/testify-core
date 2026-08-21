package com.zcomini.backend.shared.exception;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<Object> details;

    public BusinessException(HttpStatus status, String message) {
        this(status, ApiErrorCode.defaultForStatus(status.value()), message, List.of());
    }

    public BusinessException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public BusinessException(HttpStatus status, String message, List<?> details) {
        this(status, ApiErrorCode.defaultForStatus(status.value()), message, details);
    }

    public BusinessException(HttpStatus status, String code, String message, List<?> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public List<Object> getDetails() {
        return details;
    }
}

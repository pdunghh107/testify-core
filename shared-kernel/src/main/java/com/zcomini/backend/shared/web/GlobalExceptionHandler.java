package com.zcomini.backend.shared.web;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.zcomini.backend.shared.api.ErrorMessageSanitizer;
import com.zcomini.backend.shared.api.dto.ApiError;
import com.zcomini.backend.shared.api.dto.ApiErrorDetail;
import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;
import com.zcomini.backend.shared.tenant.RequestContext;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        private final String serviceName;

        public GlobalExceptionHandler(@Value("${spring.application.name:unknown-service}") String serviceName) {
                this.serviceName = serviceName;
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
                logBusinessException(ex, request);
                return build(
                                ex.getStatus(),
                                ex.getCode(),
                                ErrorMessageSanitizer.sanitizeClientMessage(ex.getMessage(),
                                                ErrorMessageSanitizer.getDefaultMessageForStatus(ex.getStatus())),
                                request,
                                sanitizeDetails(ex.getDetails()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<ApiErrorDetail> details = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> new ApiErrorDetail(
                                                "COMMON.FIELD_INVALID",
                                                error.getField(),
                                                error.getDefaultMessage()))
                                .toList();
                return build(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.BAD_REQUEST),
                                request, details);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
                List<ApiErrorDetail> details = ex.getConstraintViolations()
                                .stream()
                                .map(violation -> new ApiErrorDetail(
                                                "COMMON.FIELD_INVALID",
                                                violation.getPropertyPath().toString(),
                                                violation.getMessage()))
                                .toList();
                return build(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.BAD_REQUEST),
                                request, details);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                ApiErrorCode.BAD_REQUEST.value(),
                                ErrorMessageSanitizer.sanitizeClientMessage(ex.getMessage(),
                                                ErrorMessageSanitizer
                                                                .getDefaultMessageForStatus(HttpStatus.BAD_REQUEST)),
                                request,
                                List.of());
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.INTERNAL_SERVER_ERROR),
                                request,
                                List.of());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                log.warn("[handleDataIntegrity] : Vi phạm toàn vẹn dữ liệu tại URI {} [RequestId: {}]",
                                request.getRequestURI(),
                                RequestContext.getRequestId(), ex);
                return build(
                                HttpStatus.CONFLICT,
                                ApiErrorCode.CONFLICT.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.CONFLICT),
                                request,
                                List.of(ex.getMostSpecificCause().getMessage()));
        }

        @ExceptionHandler({
                        DataAccessException.class,
                        JpaSystemException.class,
                        TransactionSystemException.class,
                        PersistenceException.class
        })
        public ResponseEntity<ApiError> handleDatabase(Exception ex, HttpServletRequest request) {
                log.error("[handleDatabase] : Lỗi cơ sở dữ liệu [{}] tại URI {} [RequestId: {}]",
                                ex.getClass().getName(), request.getRequestURI(),
                                RequestContext.getRequestId(), ex);
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.INTERNAL_SERVER_ERROR),
                                request,
                                List.of(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName()));
        }

        @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
        public ResponseEntity<ApiError> handleResponseStatus(org.springframework.web.server.ResponseStatusException ex,
                        HttpServletRequest request) {
                HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
                return build(
                                status,
                                ApiErrorCode.defaultForStatus(status.value()),
                                ErrorMessageSanitizer.sanitizeClientMessage(
                                                ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(),
                                                ErrorMessageSanitizer.getDefaultMessageForStatus(status)),
                                request,
                                List.of());
        }

        @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
        public ResponseEntity<ApiError> handleNotFound(Exception ex, HttpServletRequest request) {
                log.warn("[handleNotFound] : Không tìm thấy tài nguyên [{}] tại URI {} [RequestId: {}]",
                                ex.getClass().getSimpleName(), request.getRequestURI(),
                                RequestContext.getRequestId());
                return build(
                                HttpStatus.NOT_FOUND,
                                ApiErrorCode.NOT_FOUND.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.NOT_FOUND),
                                request,
                                List.of());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleFallback(Exception ex, HttpServletRequest request) {
                log.error("[handleFallback] : Lỗi không xác định [{}] tại URI {} [RequestId: {}]: {}",
                                ex.getClass().getName(), request.getRequestURI(),
                                RequestContext.getRequestId(), ex.getMessage(), ex);
                if (ErrorMessageSanitizer.isDatabaseException(ex)) {
                        return build(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        ApiErrorCode.INTERNAL_SERVER_ERROR.value(),
                                        ErrorMessageSanitizer
                                                        .getDefaultMessageForStatus(HttpStatus.INTERNAL_SERVER_ERROR),
                                        request,
                                        List.of("Fallback DB Error: " + (ex.getMessage() != null ? ex.getMessage()
                                                        : ex.getClass().getName())));
                }
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.INTERNAL_SERVER_ERROR),
                                request,
                                List.of("Fallback Internal Error: " + (ex.getMessage() != null ? ex.getMessage()
                                                : ex.getClass().getName())));
        }

        private ResponseEntity<ApiError> build(HttpStatus status,
                        String code,
                        String message,
                        HttpServletRequest request,
                        List<?> details) {
                ApiError body = new ApiError(
                                Instant.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                code,
                                message,
                                request.getRequestURI(),
                                RequestContext.getRequestId(),
                                serviceName,
                                details == null ? List.of() : new java.util.ArrayList<>(details));
                return ResponseEntity.status(status).body(body);
        }

        private void logBusinessException(BusinessException ex, HttpServletRequest request) {
                if (ex.getStatus().is5xxServerError()) {
                        log.error("[handleBusiness] : Lỗi nghiệp vụ nghiêm trọng [{}] tại URI {} [RequestId: {}]: {}",
                                        ex.getCode(), request.getRequestURI(),
                                        RequestContext.getRequestId(), ex.getMessage(), ex);
                        return;
                }
                log.warn("[handleBusiness] : Lỗi nghiệp vụ [{}] tại URI {} [RequestId: {}]: {}", ex.getCode(),
                                request.getRequestURI(),
                                RequestContext.getRequestId(), ex.getMessage());
        }

        private List<Object> sanitizeDetails(List<?> details) {
                return details == null
                                ? List.of()
                                : details.stream()
                                                .map(detail -> {
                                                        if (detail instanceof String s) {
                                                                return ErrorMessageSanitizer.sanitizeClientMessage(s,
                                                                                ErrorMessageSanitizer
                                                                                                .getDefaultMessageForStatus(
                                                                                                                HttpStatus.BAD_REQUEST));
                                                        } else if (detail instanceof ApiErrorDetail d) {
                                                                return new ApiErrorDetail(
                                                                                d.code(),
                                                                                d.field(),
                                                                                ErrorMessageSanitizer
                                                                                                .sanitizeClientMessage(d
                                                                                                                .message(),
                                                                                                                ErrorMessageSanitizer
                                                                                                                                .getDefaultMessageForStatus(
                                                                                                                                                HttpStatus.BAD_REQUEST)));
                                                        }
                                                        return detail;
                                                })
                                                .toList();
        }
}

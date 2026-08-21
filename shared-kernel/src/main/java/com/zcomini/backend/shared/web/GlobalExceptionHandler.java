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

/**
 * Lớp trung tâm xử lý toàn bộ các ngoại lệ (Exception) phát sinh trong quá
 * trình thực thi của ứng dụng.
 * <p>
 * Đóng vai trò như một "lưới lọc" cuối cùng trước khi response được trả về cho
 * Client.
 * Nhiệm vụ chính của lớp này là:
 * <ul>
 * <li>Chuẩn hóa mọi lỗi về một định dạng duy nhất là {@link ApiError}.</li>
 * <li>Bảo mật thông tin hệ thống bằng cách sử dụng
 * {@link ErrorMessageSanitizer} để che giấu các chi tiết nhạy cảm (stack trace,
 * câu lệnh SQL).</li>
 * <li>Ghi log một cách phân loại (ERROR cho lỗi hệ thống 5xx, WARN cho lỗi
 * nghiệp vụ 4xx).</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * Tên service hiện tại – populate từ spring.application.name, dùng trong
         * ApiError.service để trace lỗi ở môi trường Microservices.
         */
        private final String serviceName;

        /**
         * Khởi tạo GlobalExceptionHandler với tên dịch vụ được cấu hình.
         *
         * @param serviceName Tên của dịch vụ (microservice), lấy từ biến môi trường
         *                    {@code spring.application.name}.
         */
        public GlobalExceptionHandler(@Value("${spring.application.name:unknown-service}") String serviceName) {
                this.serviceName = serviceName;
        }

        /**
         * Bắt và xử lý các lỗi nghiệp vụ (Business Logic) do lập trình viên chủ động
         * ném ra.
         *
         * @param ex      Ngoại lệ nghiệp vụ chứa mã lỗi và thông điệp tùy chỉnh.
         * @param request Yêu cầu HTTP hiện tại để trích xuất URI.
         * @return Phản hồi chuẩn hóa chứa {@link ApiError} với HTTP status tương ứng.
         */
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

        /**
         * Bắt lỗi Validation khi dữ liệu Body không vượt qua các ràng buộc (ví
         * dụ: @Valid, @NotNull).
         *
         * @param ex      Ngoại lệ ném ra khi validation trên DTO thất bại.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 400 Bad Request và danh sách các trường
         *         bị lỗi.
         */
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

        /**
         * Bắt lỗi Validation khi tham số PathVariable hoặc RequestParam không hợp lệ.
         *
         * @param ex      Ngoại lệ ném ra khi vi phạm các constraint trên tham số hàm.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 400 Bad Request.
         */
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

        /**
         * Bắt lỗi khi có đối số (argument) truyền vào hàm không hợp lệ.
         *
         * @param ex      Ngoại lệ {@link IllegalArgumentException}.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 400 Bad Request.
         */
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

        /**
         * Bắt lỗi khi trạng thái của hệ thống hoặc đối tượng không phù hợp để thực thi.
         * Lỗi này thường do lỗi logic hệ thống, trả về 500.
         *
         * @param ex      Ngoại lệ {@link IllegalStateException}.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 500 Internal Server Error.
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.INTERNAL_SERVER_ERROR),
                                request,
                                List.of());
        }

        /**
         * Bắt lỗi khi có vi phạm toàn vẹn dữ liệu từ Database (ví dụ: trùng khóa
         * chính/khóa ngoại).
         *
         * @param ex      Ngoại lệ {@link DataIntegrityViolationException}.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 409 Conflict.
         */
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                log.warn("[handleDataIntegrity] : Vi phạm toàn vẹn dữ liệu tại URI {} [RequestId: {}]", request.getRequestURI(),
                                RequestContext.getRequestId(), ex);
                return build(
                                HttpStatus.CONFLICT,
                                ApiErrorCode.CONFLICT.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.CONFLICT),
                                request,
                                List.of(ex.getMostSpecificCause().getMessage()));
        }

        /**
         * Bắt và gom nhóm tất cả các lỗi liên quan đến kết nối, truy vấn hoặc
         * transaction của Database.
         * Trả về thông báo chung chung cho user để bảo mật cấu trúc DB.
         *
         * @param ex      Ngoại lệ liên quan đến Database/JPA.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 500 Internal Server Error.
         */
        @ExceptionHandler({
                        DataAccessException.class,
                        JpaSystemException.class,
                        TransactionSystemException.class,
                        PersistenceException.class
        })
        public ResponseEntity<ApiError> handleDatabase(Exception ex, HttpServletRequest request) {
                log.error("[handleDatabase] : Lỗi cơ sở dữ liệu [{}] tại URI {} [RequestId: {}]", ex.getClass().getName(), request.getRequestURI(),
                                RequestContext.getRequestId(), ex);
                return build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                ApiErrorCode.INTERNAL_SERVER_ERROR.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.INTERNAL_SERVER_ERROR),
                                request,
                                List.of(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName()));
        }

        /**
         * Bắt các lỗi HTTP do chính framework Spring ném ra.
         *
         * @param ex      Ngoại lệ
         *                {@link org.springframework.web.server.ResponseStatusException}.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với HTTP status tương ứng từ ngoại lệ.
         */
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

        /**
         * Xử lý trường hợp người dùng truy cập một endpoint hoặc tài nguyên không tồn
         * tại (404).
         *
         * @param ex      Ngoại lệ ném ra khi không tìm thấy Controller hoặc Resource.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 404 Not Found.
         */
        @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
        public ResponseEntity<ApiError> handleNotFound(Exception ex, HttpServletRequest request) {
                log.warn("[handleNotFound] : Không tìm thấy tài nguyên [{}] tại URI {} [RequestId: {}]", ex.getClass().getSimpleName(), request.getRequestURI(),
                                RequestContext.getRequestId());
                return build(
                                HttpStatus.NOT_FOUND,
                                ApiErrorCode.NOT_FOUND.value(),
                                ErrorMessageSanitizer.getDefaultMessageForStatus(HttpStatus.NOT_FOUND),
                                request,
                                List.of());
        }

        /**
         * Chốt chặn cuối cùng (Fallback) để hứng tất cả các ngoại lệ chưa được dự liệu
         * trước.
         * Đảm bảo ứng dụng luôn trả về JSON thay vì trang HTML lỗi của server.
         *
         * @param ex      Bất kỳ ngoại lệ nào kế thừa từ {@link Exception}.
         * @param request Yêu cầu HTTP hiện tại.
         * @return Phản hồi chuẩn hóa với status 500 Internal Server Error.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleFallback(Exception ex, HttpServletRequest request) {
                log.error("[handleFallback] : Lỗi không xác định [{}] tại URI {} [RequestId: {}]: {}", ex.getClass().getName(), request.getRequestURI(),
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

        // -------------------------------------------------------------------------
        // Internal helpers
        // -------------------------------------------------------------------------

        /**
         * Khởi tạo và đóng gói một đối tượng {@link ApiError} hoàn chỉnh.
         *
         * @param status  HTTP status cần trả về.
         * @param code    Mã định danh lỗi.
         * @param message Thông điệp lỗi an toàn (đã được sanitize).
         * @param request Yêu cầu HTTP hiện tại.
         * @param details Danh sách chi tiết lỗi phụ trợ (nếu có).
         * @return Thực thể {@link ResponseEntity} chứa JSON của {@link ApiError}.
         */
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

        /**
         * Ghi log riêng biệt cho lỗi nghiệp vụ tùy theo mức độ nghiêm trọng (5xx hay
         * 4xx).
         */
        private void logBusinessException(BusinessException ex, HttpServletRequest request) {
                if (ex.getStatus().is5xxServerError()) {
                        log.error("[handleBusiness] : Lỗi nghiệp vụ nghiêm trọng [{}] tại URI {} [RequestId: {}]: {}", ex.getCode(), request.getRequestURI(),
                                        RequestContext.getRequestId(), ex.getMessage(), ex);
                        return;
                }
                log.warn("[handleBusiness] : Lỗi nghiệp vụ [{}] tại URI {} [RequestId: {}]: {}", ex.getCode(), request.getRequestURI(),
                                RequestContext.getRequestId(), ex.getMessage());
        }

        /**
         * Làm sạch (sanitize) danh sách chi tiết lỗi để tránh lộ thông tin nội bộ.
         */
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

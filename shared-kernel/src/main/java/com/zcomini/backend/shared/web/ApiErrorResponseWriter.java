package com.zcomini.backend.shared.web;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.zcomini.backend.shared.api.ErrorMessageSanitizer;
import com.zcomini.backend.shared.api.dto.ApiError;
import com.zcomini.backend.shared.tenant.RequestContext;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Lớp tiện ích (utility) nội bộ để ghi trực tiếp đối tượng {@link ApiError} vào
 * {@link HttpServletResponse}.
 * <p>
 * Lớp này được sử dụng trong các Filter (ví dụ: Security Filter) khi Request
 * chưa lọt tới
 * Controller hoặc GlobalExceptionHandler. Nó giúp đảm bảo mọi lỗi trả về từ
 * Filter cũng tuân thủ
 * chính xác định dạng chuẩn của toàn hệ thống.
 */
final class ApiErrorResponseWriter {

    private ApiErrorResponseWriter() {
    }

    /**
     * Ghi một lỗi định dạng JSON vào HTTP Response.
     *
     * @param objectMapper ObjectMapper dùng để parse object thành JSON.
     * @param serviceName  Tên của microservice hiện tại.
     * @param response     Luồng phản hồi HTTP gốc.
     * @param status       Mã trạng thái HTTP (ví dụ: 401 UNAUTHORIZED).
     * @param code         Mã lỗi nghiệp vụ hoặc mã chung của hệ thống.
     * @param message      Thông điệp lỗi. Sẽ được sanitize để đảm bảo an toàn.
     * @param path         Đường dẫn API gây ra lỗi.
     * @param details      Danh sách các chi tiết lỗi cụ thể (nếu có).
     * @throws IOException Nếu có lỗi trong quá trình ghi stream.
     */
    static void write(ObjectMapper objectMapper,
            String serviceName,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message,
            String path,
            List<String> details) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                ErrorMessageSanitizer.sanitizeClientMessage(message,
                        ErrorMessageSanitizer.getDefaultMessageForStatus(status)),
                path,
                RequestContext.getRequestId(),
                serviceName,
                details == null ? List.of() : List.copyOf(details));

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}

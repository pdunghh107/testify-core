package com.zcomini.backend.shared.api;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;

public final class ErrorMessageSanitizer {

    private static final int MAX_CLIENT_MESSAGE_LENGTH = 240;

    private static final List<String> SENSITIVE_MARKERS = List.of(
            "select ",
            "insert ",
            "update ",
            "delete ",
            " from ",
            " where ",
            " join ",
            "sqlstate",
            "sql [",
            "jdbc",
            "hibernate",
            "psqlexception",
            "postgresql",
            "preparedstatement",
            "constraint [",
            "duplicate key value",
            "violates unique constraint",
            "could not execute statement",
            "syntax error at or near",
            "org.springframework.dao",
            "org.hibernate",
            "jakarta.persistence",
            "java.sql"
    );

    private ErrorMessageSanitizer() {
    }

    public static String sanitizeClientMessage(String message, String fallback) {
        if (!StringUtils.hasText(message)) {
            return fallback;
        }
        String trimmed = message.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (trimmed.length() > MAX_CLIENT_MESSAGE_LENGTH || containsSensitiveMarker(normalized)) {
            return fallback;
        }
        return trimmed;
    }

    public static boolean isDatabaseException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName().toLowerCase(Locale.ROOT);
            if (className.contains("sql")
                    || className.contains("jdbc")
                    || className.contains("hibernate")
                    || className.contains("persistence")
                    || className.contains("dataaccess")
                    || className.contains("datasource")) {
                return true;
            }
            if (containsSensitiveMarker(String.valueOf(current.getMessage()).toLowerCase(Locale.ROOT))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean containsSensitiveMarker(String value) {
        for (String marker : SENSITIVE_MARKERS) {
            if (value.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Map một mã trạng thái HTTP sang thông điệp tiếng Việt thân thiện mặc định.
     *
     * @param status Trạng thái HTTP.
     * @return Thông báo lỗi tương ứng.
     */
    public static String getDefaultMessageForStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Yêu cầu chưa hợp lệ.";
            case UNAUTHORIZED -> "Cần xác thực để tiếp tục.";
            case PAYMENT_REQUIRED -> "Yêu cầu thanh toán để tiếp tục.";
            case FORBIDDEN -> "Bạn không có quyền thực hiện thao tác này.";
            case NOT_FOUND -> "Không tìm thấy dữ liệu yêu cầu.";
            case METHOD_NOT_ALLOWED -> "Phương thức HTTP không được hỗ trợ.";
            case NOT_ACCEPTABLE -> "Định dạng dữ liệu không được chấp nhận.";
            case PROXY_AUTHENTICATION_REQUIRED -> "Cần xác thực proxy.";
            case REQUEST_TIMEOUT -> "Yêu cầu đã hết thời gian chờ.";
            case CONFLICT -> "Dữ liệu đang bị trùng hoặc xung đột với thông tin hiện có.";
            case GONE -> "Tài nguyên không còn tồn tại.";
            case LENGTH_REQUIRED -> "Yêu cầu cần xác định độ dài nội dung.";
            case PRECONDITION_FAILED -> "Không thỏa mãn điều kiện tiên quyết.";
            case PAYLOAD_TOO_LARGE -> "Dữ liệu tải lên quá lớn.";
            case URI_TOO_LONG -> "Đường dẫn URI quá dài.";
            case UNSUPPORTED_MEDIA_TYPE -> "Định dạng dữ liệu tải lên không được hỗ trợ.";
            case REQUESTED_RANGE_NOT_SATISFIABLE -> "Phạm vi yêu cầu dữ liệu không hợp lệ.";
            case EXPECTATION_FAILED -> "Yêu cầu không đạt kỳ vọng của máy chủ.";
            case I_AM_A_TEAPOT -> "Tôi là một ấm trà (Lỗi giả định).";
            case UNPROCESSABLE_ENTITY -> "Không thể xử lý dữ liệu yêu cầu hiện tại.";
            case LOCKED -> "Tài nguyên đang bị khóa.";
            case FAILED_DEPENDENCY -> "Phụ thuộc thất bại.";
            case TOO_EARLY -> "Yêu cầu gửi quá sớm.";
            case UPGRADE_REQUIRED -> "Yêu cầu nâng cấp giao thức.";
            case PRECONDITION_REQUIRED -> "Yêu cầu bắt buộc phải có điều kiện tiên quyết.";
            case TOO_MANY_REQUESTS -> "Hệ thống đang quá tải yêu cầu, vui lòng thử lại sau.";
            case REQUEST_HEADER_FIELDS_TOO_LARGE -> "Thông tin tiêu đề (Header) quá lớn.";
            case UNAVAILABLE_FOR_LEGAL_REASONS -> "Không khả dụng vì lý do pháp lý.";
            case INTERNAL_SERVER_ERROR -> "Có lỗi hệ thống. Vui lòng thử lại sau.";
            case NOT_IMPLEMENTED -> "Chức năng này chưa được triển khai.";
            case BAD_GATEWAY -> "Dịch vụ phụ trợ đang tạm thời không khả dụng.";
            case SERVICE_UNAVAILABLE -> "Dịch vụ đang tạm thời không khả dụng.";
            case GATEWAY_TIMEOUT -> "Hết thời gian chờ phản hồi từ dịch vụ phụ trợ.";
            case HTTP_VERSION_NOT_SUPPORTED -> "Phiên bản giao thức HTTP không được hỗ trợ.";
            case VARIANT_ALSO_NEGOTIATES -> "Lỗi đàm phán nội dung máy chủ.";
            case INSUFFICIENT_STORAGE -> "Không đủ không gian lưu trữ trên máy chủ.";
            case LOOP_DETECTED -> "Phát hiện vòng lặp vô hạn trong quá trình xử lý.";
            case NOT_EXTENDED -> "Cần mở rộng thêm thông tin.";
            case NETWORK_AUTHENTICATION_REQUIRED -> "Yêu cầu xác thực mạng để tiếp tục.";
            default -> "Có lỗi hệ thống. Vui lòng thử lại sau.";
        };
    }
}

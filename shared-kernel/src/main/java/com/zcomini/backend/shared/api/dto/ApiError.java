package com.zcomini.backend.shared.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Cấu trúc đóng gói (envelope) dữ liệu chuẩn cho tất cả các phản hồi lỗi từ
 * Testify Microservices.
 * <p>
 * Bất cứ khi nào hệ thống gặp ngoại lệ (ngoại lệ nghiệp vụ, ngoại lệ xác thực,
 * lỗi hệ thống, v.v.),
 * bộ xử lý lỗi tập trung sẽ chuyển đổi ngoại lệ đó thành cấu trúc này. Điều này
 * giúp
 * Frontend luôn nhận được một đối tượng JSON nhất quán cho mọi loại lỗi, dễ
 * dàng ánh xạ
 * và hiển thị thông báo cho người dùng cuối.
 *
 * <pre>{@code
 * {
 * "timestamp": "2026-04-14T08:30:00Z",
 * "status": 404,
 * "error": "Not Found",
 * "code": "ORDER.NOT_FOUND",
 * "message": "Requested resource was not found.",
 * "path": "/api/v1/orders/abc",
 * "traceId": "3fa2b1c8-...",
 * "service": "order-service",
 * "details": []
 * }
 * }</pre>
 *
 * @param timestamp Thời điểm xảy ra lỗi theo chuẩn UTC ISO-8601.
 * @param status Mã trạng thái HTTP (Ví dụ: 400, 404, 500).
 * @param error Cụm từ mô tả ngắn gọn về mã trạng thái HTTP (Ví dụ: "Not Found",
 * "Bad Request").
 * @param code Mã định danh lỗi nghiệp vụ cụ thể theo từng namespace (Ví dụ:
 * "ORDER.NOT_FOUND").
 * Xem thêm {@link com.zcomini.backend.shared.api.enums.ApiErrorCode}.
 * @param message Thông điệp giải thích chi tiết về lỗi, an toàn để có thể hiển
 * thị trực tiếp cho người dùng.
 * @param path Đường dẫn URI của API gây ra lỗi.
 * @param traceId Mã Correlation/Trace ID duy nhất cho request, dùng để tra cứu
 * log phân tán.
 * @param service Tên của microservice nơi phát sinh ra lỗi (được lấy từ {@code
 * spring.application.name}).
 * @param details Danh sách chứa các thông tin bổ sung về lỗi, thường được sử
 * dụng cho các lỗi xác thực dữ liệu (validation failures).
 */
public record ApiError(
                Instant timestamp,
                int status,
                String error,
                String code,
                String message,
                String path,
                String traceId,
                String service,
                List<Object> details) {
}

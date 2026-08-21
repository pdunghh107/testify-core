package com.zcomini.backend.shared.api.enums;

/**
 * Định nghĩa bảng mã lỗi chuẩn toàn cục cho các API của Testify Microservices.
 * <p>
 * Enum này tập hợp tất cả các mã trạng thái HTTP tiêu chuẩn được hệ thống sử dụng,
 * đồng thời cung cấp cơ chế để định nghĩa các mã lỗi nghiệp vụ đặc thù (như lỗi xác thực) 
 * mà vẫn tuân thủ chặt chẽ cấu trúc phản hồi của API. Bằng cách sử dụng mã HTTP mặc định 
 * (ví dụ: {@code "404"}) thay vì chuỗi tên (như {@code "NOT_FOUND"}), hệ thống đảm bảo 
 * tính đóng-mở (Open-Closed Principle) khi có sự thay đổi tên gọi từ các phiên bản Spring.
 * 
 * @see com.zcomini.backend.shared.api.dto.ApiError
 */
public enum ApiErrorCode {

    BAD_REQUEST(400, "400"),
    UNAUTHORIZED(401, "401"),
    PAYMENT_REQUIRED(402, "402"),
    FORBIDDEN(403, "403"),
    NOT_FOUND(404, "404"),
    METHOD_NOT_ALLOWED(405, "405"),
    NOT_ACCEPTABLE(406, "406"),
    PROXY_AUTHENTICATION_REQUIRED(407, "407"),
    REQUEST_TIMEOUT(408, "408"),
    CONFLICT(409, "409"),
    GONE(410, "410"),
    LENGTH_REQUIRED(411, "411"),
    PRECONDITION_FAILED(412, "412"),
    PAYLOAD_TOO_LARGE(413, "413"),
    URI_TOO_LONG(414, "414"),
    UNSUPPORTED_MEDIA_TYPE(415, "415"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "416"),
    EXPECTATION_FAILED(417, "417"),
    I_AM_A_TEAPOT(418, "418"),
    UNPROCESSABLE_CONTENT(422, "422"),
    LOCKED(423, "423"),
    FAILED_DEPENDENCY(424, "424"),
    TOO_EARLY(425, "425"),
    UPGRADE_REQUIRED(426, "426"),
    PRECONDITION_REQUIRED(428, "428"),
    TOO_MANY_REQUESTS(429, "429"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "431"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "451"),

    INTERNAL_SERVER_ERROR(500, "500"),
    NOT_IMPLEMENTED(501, "501"),
    BAD_GATEWAY(502, "502"),
    SERVICE_UNAVAILABLE(503, "503"),
    GATEWAY_TIMEOUT(504, "504"),
    HTTP_VERSION_NOT_SUPPORTED(505, "505"),
    VARIANT_ALSO_NEGOTIATES(506, "506"),
    INSUFFICIENT_STORAGE(507, "507"),
    LOOP_DETECTED(508, "508"),
    NOT_EXTENDED(510, "510"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "511"),

    AUTH_TOKEN_EXPIRED(401, "AUTH.TOKEN_EXPIRED"),
    AUTH_TOKEN_INVALID(401, "AUTH.TOKEN_INVALID"),
    AUTH_TOKEN_REVOKED(401, "AUTH.TOKEN_REVOKED"),
    AUTH_REFRESH_TOKEN_INVALID(401, "AUTH.REFRESH_TOKEN_INVALID"),
    AUTH_CREDENTIALS_INVALID(401, "AUTH.CREDENTIALS_INVALID"),
    AUTH_ACCOUNT_LOCKED(423, "AUTH.ACCOUNT_LOCKED");

    private final int status;
    private final String code;

    /**
     * Khởi tạo một mã lỗi API với mã trạng thái HTTP và mã định danh lỗi.
     *
     * @param status Mã trạng thái HTTP thực tế (ví dụ: 400, 404, 500).
     * @param code   Mã định danh lỗi dạng chuỗi, được trả về cho Frontend (ví dụ: {@code "404"} hoặc {@code "AUTH.TOKEN_EXPIRED"}).
     */
    ApiErrorCode(int status, String code) {
        this.status = status;
        this.code = code;
    }

    /**
     * Lấy mã trạng thái HTTP chuẩn của lỗi hiện tại.
     * <p>
     * Dùng để thiết lập HTTP Status cho Response gửi về cho Client.
     *
     * @return Mã trạng thái HTTP (ví dụ: 404, 500).
     */
    public int status() {
        return status;
    }

    /**
     * Lấy chuỗi định danh mã lỗi tương ứng để trả về trong payload của API.
     * <p>
     * Thay vì trả về tên của Enum (ví dụ: {@code "UNPROCESSABLE_CONTENT"}), hệ thống
     * sẽ trả về trực tiếp giá trị của trường {@code code} (như {@code "422"}). 
     * Điều này đảm bảo tính tương thích và ổn định lâu dài.
     *
     * @return Chuỗi chứa mã lỗi cụ thể (ví dụ: {@code "422"} hoặc {@code "AUTH.TOKEN_INVALID"}).
     */
    public String value() {
        return code;
    }

    /**
     * Truy xuất mã định danh lỗi mặc định (chuẩn HTTP) tương ứng với một mã trạng thái HTTP cho trước.
     * <p>
     * Phương thức này đóng vai trò như một cơ chế dự phòng (fallback) khi một ngoại lệ nghiệp vụ 
     * (BusinessException) không cung cấp mã lỗi cụ thể. Hệ thống sẽ tự động đối chiếu mã HTTP 
     * được truyền vào và trả ra mã định danh chuẩn. Mọi mã lỗi ngoại lệ nghiệp vụ (như {@code AUTH_}) 
     * sẽ bị bỏ qua một cách an toàn nhờ thiết kế map tĩnh (switch).
     *
     * @param status Mã trạng thái HTTP cần tìm kiếm (ví dụ: 404).
     * @return Chuỗi định danh lỗi mặc định (ví dụ: {@code "404"}). Trả về {@code "500"} nếu không tìm thấy mã phù hợp.
     * @see com.zcomini.backend.shared.exception.BusinessException
     */
    public static String defaultForStatus(int status) {
        return switch (status) {
            case 400 -> BAD_REQUEST.value();
            case 401 -> UNAUTHORIZED.value();
            case 402 -> PAYMENT_REQUIRED.value();
            case 403 -> FORBIDDEN.value();
            case 404 -> NOT_FOUND.value();
            case 405 -> METHOD_NOT_ALLOWED.value();
            case 406 -> NOT_ACCEPTABLE.value();
            case 409 -> CONFLICT.value();
            case 415 -> UNSUPPORTED_MEDIA_TYPE.value();
            case 422 -> UNPROCESSABLE_CONTENT.value();
            case 429 -> TOO_MANY_REQUESTS.value();
            case 502 -> BAD_GATEWAY.value();
            case 503 -> SERVICE_UNAVAILABLE.value();
            default -> INTERNAL_SERVER_ERROR.value();
        };
    }
}

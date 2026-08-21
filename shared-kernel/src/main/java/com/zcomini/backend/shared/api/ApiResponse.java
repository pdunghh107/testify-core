package com.zcomini.backend.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.zcomini.backend.shared.api.dto.PageResponse;
import com.zcomini.backend.shared.api.enums.ApiSuccessCode;
import com.zcomini.backend.shared.tenant.RequestContext;

/**
 * Envelope chuẩn định dạng dữ liệu trả về cho mọi API thành công của hệ thống
 * Testify.
 * <p>
 * Class này đóng vai trò là một lớp vỏ bọc (wrapper) thống nhất, đảm bảo mọi
 * phản hồi HTTP
 * (trừ các trường hợp lỗi sẽ được xử lý bởi GlobalExceptionHandler) đều có cấu
 * trúc JSON nhất quán.
 * Điều này giúp Frontend dễ dàng parse dữ liệu, quản lý trạng thái, và hiển thị
 * thông báo.
 * <p>
 * Hệ thống cũng tự động nhúng {@code requestId} (Correlation ID) thông qua
 * {@link RequestContext}
 * vào mỗi response để hỗ trợ việc truy vết (tracing) log xuyên suốt các
 * microservices.
 *
 * <h3>1. Trường hợp trả về dữ liệu thành công (ok):</h3>
 * <pre>{@code
 * {
 * "success": true,
 * "code": "OK",
 * "message": "Thành công",
 * "data": { "id": "1", "name": "Fiza" },
 * "requestId": "3fa2b1c8-..."
 * }
 * }</pre>
 *
 * <h3>2. Trường hợp tạo mới dữ liệu thành công (created):</h3>
 * <pre>{@code
 * {
 * "success": true,
 * "code": "CREATED",
 * "message": "Thành công",
 * "data": { "id": "2" },
 * "requestId": "3fa2b1c8-..."
 * }
 * }</pre>
 *
 * <h3>3. Trường hợp trả về danh sách phân trang (paged):</h3>
 * <pre>{@code
 * {
 * "success": true,
 * "code": "PAGED",
 * "message": "Thành công",
 * "data": { "content": [...], "page": 1, "size": 10, "totalElements": 50 },
 * "requestId": "3fa2b1c8-..."
 * }
 * }</pre>
 *
 * <h3>4. Trường hợp chỉ thực hiện hành động, không có data (message):</h3>
 * <pre>{@code
 * {
 * "success": true,
 * "code": "MESSAGE",
 * "message": "Đăng xuất thành công",
 * "requestId": "3fa2b1c8-..."
 * }
 * }</pre>
 *
 * <h3>5. Trường hợp trả về mã code tùy chỉnh (custom code):</h3>
 * <pre>{@code
 * {
 * "success": true,
 * "code": "USER_UPDATED",
 * "message": "Thành công",
 * "data": { "id": "1", "status": "ACTIVE" },
 * "requestId": "3fa2b1c8-..."
 * }
 * }</pre>
 *
 * <h3>6. Trường hợp trả về mã code và thông báo tùy chỉnh (custom
 * message):</h3>
 * <pre>{@code
 * {
 * "success": true,
 * "code": "PASSWORD_CHANGED",
 * "message": "Mật khẩu của bạn đã được thay đổi an toàn.",
 * "requestId": "3fa2b1c8-..."
 * }
 * }</pre>
 *
 * @param success Cờ đánh dấu trạng thái của request. Luôn là {@code true} đối
 * với các response thành công.
 * @param code Mã kết quả nghiệp vụ (Ví dụ: "OK", "CREATED"). Giúp Frontend dễ
 * bắt sự kiện hơn dựa vào HTTP status.
 * @param message Thông điệp hiển thị cho người dùng. Nếu không cung cấp, hệ
 * thống tự động gán là "Thành công".
 * @param data Dữ liệu payload cốt lõi của API. Nếu API chỉ xử lý thao tác (như
 * xóa), trường này có thể là {@code null} và sẽ bị ẩn khỏi JSON.
 * @param requestId Mã định danh duy nhất của chuỗi request, trích xuất từ
 * Header, phục vụ cho việc truy vết log.
 * @param <T> Kiểu dữ liệu thực tế của payload được trả về bên trong thuộc tính
 * {@code data}.
 * Sử dụng {@code Void} nếu API không trả về bất kỳ dữ liệu nào.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        String requestId) {

    /**
     * Hàm trung tâm (Builder) đảm nhận việc khởi tạo đối tượng phản hồi.
     * <p>
     * Tập trung logic xử lý thông điệp mặc định tại một nơi duy nhất.
     * Hàm này kiểm tra tham số {@code message}, nếu bị null hoặc rỗng, nó sẽ gán
     * giá trị mặc định là "Thành công".
     * Cuối cùng, nó nhúng Correlation ID từ luồng thực thi hiện tại (ThreadLocal)
     * qua {@code RequestContext}.
     *
     * @param success Trạng thái xử lý của API (mặc định luôn là {@code true} đối
     *                với class này).
     * @param code    Mã định danh nghiệp vụ (chuẩn hóa qua {@link ApiSuccessCode}).
     * @param message Thông điệp mô tả chi tiết (có thể {@code null}).
     * @param data    Dữ liệu cần gửi về cho Client (có thể {@code null}).
     * @param <T>     Kiểu dữ liệu của payload.
     * @return Đối tượng {@link ApiResponse} hoàn chỉnh sẵn sàng chuyển đổi sang
     *         JSON.
     */
    private static <T> ApiResponse<T> buildResponse(boolean success, ApiSuccessCode code, String message, T data) {
        String finalMessage = (message == null || message.trim().isEmpty()) ? "Thành công" : message;
        return new ApiResponse<>(success, code.value(), finalMessage, data, RequestContext.getRequestId());
    }

    /**
     * Tạo phản hồi thành công cơ bản chỉ với dữ liệu payload.
     * <p>
     * Mã nghiệp vụ được gán mặc định là "OK" và thông điệp tự động điền là "Thành
     * công".
     *
     * @param data Dữ liệu payload cốt lõi.
     * @param <T>  Kiểu dữ liệu của payload.
     * @return Khung chứa phản hồi.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return buildResponse(true, ApiSuccessCode.OK, null, data);
    }

    /**
     * Tạo phản hồi thành công trả về cả dữ liệu payload và thông điệp tùy chỉnh.
     *
     * @param data    Dữ liệu payload cốt lõi.
     * @param message Thông báo mô tả hoặc lời chúc hiển thị cho Frontend.
     * @param <T>     Kiểu dữ liệu của payload.
     * @return Khung chứa phản hồi.
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return buildResponse(true, ApiSuccessCode.OK, message, data);
    }

    /**
     * Tạo phản hồi xác nhận một tài nguyên mới vừa được tạo (tương ứng HTTP 201
     * Created).
     * <p>
     * Mã nghiệp vụ được gán là "CREATED". Thông điệp mặc định là "Thành công".
     *
     * @param data Dữ liệu của tài nguyên vừa tạo (thường chứa ID mới sinh).
     * @param <T>  Kiểu dữ liệu của tài nguyên.
     * @return Khung chứa phản hồi.
     */
    public static <T> ApiResponse<T> created(T data) {
        return buildResponse(true, ApiSuccessCode.CREATED, null, data);
    }

    /**
     * Tạo phản hồi dành cho các thao tác không cần trả dữ liệu (chỉ cần báo thành
     * công).
     * <p>
     * Ví dụ áp dụng cho các API: logout, đổi mật khẩu, vô hiệu hóa tài khoản, xóa
     * bài viết.
     *
     * @param message Thông điệp phản hồi báo cáo kết quả hành động.
     * @return Khung chứa phản hồi không có body {@code data}.
     */
    public static ApiResponse<Void> message(String message) {
        return buildResponse(true, ApiSuccessCode.MESSAGE, message, null);
    }

    /**
     * Tạo phản hồi dành riêng cho việc lấy danh sách có phân trang.
     * <p>
     * Gói gọn đối tượng {@link PageResponse} vào trong {@code data} để giữ cấu trúc
     * JSON thống nhất.
     *
     * @param page Đối tượng phản hồi phân trang chứa metadata (page, size, total)
     *             và mảng dữ liệu.
     * @param <T>  Kiểu dữ liệu của phần tử trong danh sách.
     * @return Khung chứa phản hồi chứa đối tượng phân trang.
     */
    public static <T> ApiResponse<PageResponse<T>> paged(PageResponse<T> page) {
        return buildResponse(true, ApiSuccessCode.PAGED, null, page);
    }

    /**
     * Tạo phản hồi không có dữ liệu, sử dụng mã tùy chỉnh từ tập
     * {@link ApiSuccessCode}.
     *
     * @param code Mã trạng thái tùy chỉnh được khai báo trong enum.
     * @return Khung chứa phản hồi với mã tùy chỉnh.
     */
    public static ApiResponse<Void> ok(ApiSuccessCode code) {
        return buildResponse(true, code, null, null);
    }

    /**
     * Tạo phản hồi chứa dữ liệu, sử dụng mã tùy chỉnh từ tập
     * {@link ApiSuccessCode}.
     *
     * @param code Mã trạng thái tùy chỉnh được khai báo trong enum.
     * @param data Dữ liệu payload cốt lõi.
     * @param <T>  Kiểu dữ liệu của payload.
     * @return Khung chứa phản hồi với mã tùy chỉnh.
     */
    public static <T> ApiResponse<T> ok(ApiSuccessCode code, T data) {
        return buildResponse(true, code, null, data);
    }

    /**
     * Tạo phản hồi đầy đủ bao gồm dữ liệu, mã tùy chỉnh và thông báo tùy chỉnh.
     *
     * @param code    Mã trạng thái tùy chỉnh được khai báo trong enum.
     * @param data    Dữ liệu payload cốt lõi.
     * @param message Thông báo mô tả chi tiết.
     * @param <T>     Kiểu dữ liệu của payload.
     * @return Khung chứa phản hồi đầy đủ thông tin nhất.
     */
    public static <T> ApiResponse<T> ok(ApiSuccessCode code, T data, String message) {
        return buildResponse(true, code, message, data);
    }

    /**
     * Tạo phản hồi báo hiệu tạo tài nguyên thành công, kết hợp mã tùy chỉnh.
     *
     * @param code Mã trạng thái tùy chỉnh liên quan đến việc tạo mới.
     * @param data Dữ liệu của tài nguyên vừa tạo.
     * @param <T>  Kiểu dữ liệu của tài nguyên.
     * @return Khung chứa phản hồi.
     */
    public static <T> ApiResponse<T> created(ApiSuccessCode code, T data) {
        return buildResponse(true, code, null, data);
    }

    /**
     * Tạo phản hồi dành cho thao tác không trả dữ liệu, kết hợp mã code và thông
     * báo tùy chỉnh.
     *
     * @param code    Mã trạng thái tùy chỉnh.
     * @param message Thông điệp phản hồi.
     * @return Khung chứa phản hồi không có body {@code data}.
     */
    public static ApiResponse<Void> message(ApiSuccessCode code, String message) {
        return buildResponse(true, code, message, null);
    }

    /**
     * Tạo phản hồi phân trang sử dụng mã trạng thái tùy chỉnh.
     *
     * @param code Mã trạng thái tùy chỉnh.
     * @param page Đối tượng phản hồi phân trang.
     * @param <T>  Kiểu dữ liệu của phần tử trong danh sách.
     * @return Khung chứa phản hồi chứa đối tượng phân trang.
     */
    public static <T> ApiResponse<PageResponse<T>> paged(ApiSuccessCode code, PageResponse<T> page) {
        return buildResponse(true, code, null, page);
    }
}

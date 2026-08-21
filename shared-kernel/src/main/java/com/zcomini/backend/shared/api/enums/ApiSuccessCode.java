package com.zcomini.backend.shared.api.enums;

/**
 * Tập hợp các mã trạng thái chuẩn (Standard Success Codes) cho các phản hồi API
 * thành công.
 * <p>
 * Trong mô hình <b>Hybrid Envelope</b> của Testify, enum này định nghĩa các mã
 * cốt lõi
 * để định tuyến trạng thái xử lý về phía Frontend, giúp Frontend dễ dàng nhận
 * diện
 * kết quả hành động (như tạo mới, lấy danh sách phân trang, hay chỉ hiển thị
 * thông báo)
 * mà không cần phải parse sâu vào nội dung của {@code data}.
 * <p>
 * Hệ thống ưu tiên sự tối giản bằng cách loại bỏ các mã tùy chỉnh (custom
 * codes)
 * và chỉ duy trì 4 trạng thái cốt lõi. Lớp
 * {@link com.zcomini.backend.shared.web.ApiEnvelopeResponseBodyAdvice}
 * sẽ tự động sử dụng các mã này để bọc (wrap) kết quả trả về từ Controller.
 *
 * @see com.zcomini.backend.shared.api.ApiResponse
 * @see com.zcomini.backend.shared.web.ApiEnvelopeResponseBodyAdvice
 */
public enum ApiSuccessCode {

    /**
     * Xử lý thành công các yêu cầu truy xuất hoặc cập nhật dữ liệu thông thường
     * (HTTP 200).
     */
    OK("OK"),

    /**
     * Khởi tạo tài nguyên mới thành công trên hệ thống (tương ứng với HTTP 201).
     */
    CREATED("CREATED"),

    /**
     * Trả về danh sách dữ liệu có phân trang, đi kèm với metadata (tổng số trang,
     * số phần tử).
     */
    PAGED("PAGED"),

    /**
     * Thao tác hoàn tất và chỉ trả về một chuỗi thông báo, không kèm theo dữ liệu
     * payload.
     */
    MESSAGE("MESSAGE");

    private final String value;

    /**
     * Khởi tạo một mã thành công với giá trị chuỗi tương ứng.
     *
     * @param value Giá trị chuỗi sẽ được serialize vào JSON trả về cho Client,
     *              không được {@code null}.
     */
    ApiSuccessCode(String value) {
        this.value = value;
    }

    /**
     * Truy xuất giá trị chuỗi nguyên bản của mã thành công.
     *
     * @return Chuỗi đại diện cho mã trạng thái (Ví dụ: "OK", "CREATED").
     */
    public String value() {
        return value;
    }
}

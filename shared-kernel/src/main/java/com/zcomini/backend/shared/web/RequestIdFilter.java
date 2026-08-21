package com.zcomini.backend.shared.web;

import java.io.IOException;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zcomini.backend.shared.tenant.HeaderNames;
import com.zcomini.backend.shared.tenant.RequestContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Bộ lọc (Filter) đảm nhiệm việc quản lý và lan truyền mã định danh duy nhất
 * (Request ID)
 * cho mỗi yêu cầu HTTP đi vào hệ thống.
 * <p>
 * Luồng hoạt động:
 * <ol>
 * <li>Trích xuất {@code X-Request-Id} từ HTTP Header của request gửi đến.</li>
 * <li>Nếu Client không truyền lên, hệ thống sẽ tự động khởi tạo một UUID
 * mới.</li>
 * <li>Lưu trữ mã này vào {@link RequestContext} (ThreadLocal) để các tầng bên
 * dưới (Service, Log) có thể truy cập.</li>
 * <li>Nhúng ngược mã này vào HTTP Response Header để trả về cho Client, phục vụ
 * cho việc truy vết và debug.</li>
 * </ol>
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    /**
     * Thực thi logic lọc cho mỗi request.
     *
     * @param request     Đối tượng chứa thông tin HTTP Request gửi đến.
     * @param response    Đối tượng chứa thông tin HTTP Response sẽ trả về.
     * @param filterChain Chuỗi các filter tiếp theo trong vòng đời xử lý request.
     * @throws ServletException Nếu có lỗi xảy ra trong quá trình xử lý Servlet.
     * @throws IOException      Nếu có lỗi I/O xảy ra.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestId = request.getHeader(HeaderNames.REQUEST_ID);

        // Sinh mới nếu Client không cung cấp
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }

        // Lưu vào Context để dùng chung trong toàn bộ luồng xử lý (Thread)
        RequestContext.setRequestId(requestId);

        // Đính kèm vào Response Header để trả về cho Client
        response.setHeader(HeaderNames.REQUEST_ID, requestId);

        // Chuyển quyền điều khiển cho Filter tiếp theo
        filterChain.doFilter(request, response);
    }
}

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

@Component
public class RequestIdFilter extends OncePerRequestFilter {

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

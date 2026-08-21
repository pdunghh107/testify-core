package com.zcomini.backend.shared.web;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.zcomini.backend.shared.api.dto.ApiError;
import com.zcomini.backend.shared.api.ApiResponse;
import com.zcomini.backend.shared.api.dto.PageResponse;

@RestControllerAdvice
public class ApiEnvelopeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getDeclaringClass().getName().startsWith("org.springdoc")) {
            return false;
        }
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        if (body == null || selectedContentType == null
                || !MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {
            return body;
        }

        if (body instanceof ApiResponse<?> || body instanceof ApiError) {
            return body;
        }

        if (body instanceof Page<?> page) {
            return ApiResponse.paged(PageResponse.from(page));
        }

        if (body instanceof PageResponse<?> pageResponse) {
            return ApiResponse.paged(pageResponse);
        }

        if (body instanceof com.zcomini.backend.shared.api.dto.MessageResponse msg) {
            return ApiResponse.message(msg.message());
        }

        int status = response instanceof ServletServerHttpResponse servletResponse
                ? servletResponse.getServletResponse().getStatus()
                : 200;

        if (status == 201) {
            return ApiResponse.created(body);
        }

        return ApiResponse.ok(body);
    }
}

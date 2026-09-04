package com.zcomini.backend.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.zcomini.backend.shared.api.dto.PageResponse;
import com.zcomini.backend.shared.api.enums.ApiSuccessCode;
import com.zcomini.backend.shared.tenant.RequestContext;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        String requestId) {

    private static <T> ApiResponse<T> buildResponse(boolean success, ApiSuccessCode code, String message, T data) {
        String finalMessage = (message == null || message.trim().isEmpty()) ? "Thành công" : message;
        return new ApiResponse<>(success, code.value(), finalMessage, data, RequestContext.getRequestId());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return buildResponse(true, ApiSuccessCode.OK, null, data);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return buildResponse(true, ApiSuccessCode.OK, message, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return buildResponse(true, ApiSuccessCode.CREATED, null, data);
    }

    public static ApiResponse<Void> message(String message) {
        return buildResponse(true, ApiSuccessCode.MESSAGE, message, null);
    }

    public static <T> ApiResponse<PageResponse<T>> paged(PageResponse<T> page) {
        return buildResponse(true, ApiSuccessCode.PAGED, null, page);
    }

    public static ApiResponse<Void> ok(ApiSuccessCode code) {
        return buildResponse(true, code, null, null);
    }

    public static <T> ApiResponse<T> ok(ApiSuccessCode code, T data) {
        return buildResponse(true, code, null, data);
    }

    public static <T> ApiResponse<T> ok(ApiSuccessCode code, T data, String message) {
        return buildResponse(true, code, message, data);
    }

    public static <T> ApiResponse<T> created(ApiSuccessCode code, T data) {
        return buildResponse(true, code, null, data);
    }

    public static ApiResponse<Void> message(ApiSuccessCode code, String message) {
        return buildResponse(true, code, message, null);
    }

    public static <T> ApiResponse<PageResponse<T>> paged(ApiSuccessCode code, PageResponse<T> page) {
        return buildResponse(true, code, null, page);
    }
}

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

final class ApiErrorResponseWriter {

        private ApiErrorResponseWriter() {
        }

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

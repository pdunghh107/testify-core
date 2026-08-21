package com.zcomini.backend.testify.dto.request;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRequestDto(
                UUID folderId,
                @NotBlank(message = "Tên API không được để trống") @Size(min = 2, max = 100, message = "Tên API từ 2 đến 100 ký tự") String name,
                @NotBlank(message = "URL không được để trống") String url,
                @NotBlank(message = "Phương thức HTTP không được để trống") String method,
                Map<String, Object> headers,
                String bodyTemplate,
                UUID defaultRuleId) {
}

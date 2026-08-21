package com.zcomini.backend.testify.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateFieldConfigRequest(
        @NotBlank(message = "Tên cấu hình không được để trống") @Size(min = 2, max = 100, message = "Tên cấu hình từ 2 đến 100 ký tự") String name,
        @NotEmpty(message = "Cần ít nhất 1 từ khóa") List<@NotBlank(message = "Từ khóa không được để trống") String> containsKeywords,
        String defaultRegex
) {
}

package com.zcomini.backend.testify.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateRuleRequest(
        @NotNull(message = "Workspace ID không được để trống") UUID workspaceId,
        @NotBlank(message = "Mã cấu hình (Rule Config Code) không được để trống") String ruleConfigCode,
        @NotBlank(message = "Template Body không được để trống") String bodyTemplate
) {
}

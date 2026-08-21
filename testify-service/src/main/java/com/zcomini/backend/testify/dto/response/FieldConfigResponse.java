package com.zcomini.backend.testify.dto.response;

import com.zcomini.backend.testify.entity.FieldConfig;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FieldConfigResponse(
        UUID id,
        UUID workspaceId,
        String name,
        List<String> containsKeywords,
        String defaultRegex,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static FieldConfigResponse from(FieldConfig entity) {
        return new FieldConfigResponse(
                entity.getId(),
                entity.getWorkspace().getId(),
                entity.getName(),
                entity.getContainsKeywords(),
                entity.getDefaultRegex(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

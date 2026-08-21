package com.zcomini.backend.testify.dto.response;

import com.zcomini.backend.testify.entity.Request;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RequestResponse(
        UUID id,
        UUID workspaceId,
        UUID folderId,
        String name,
        String url,
        String method,
        Map<String, Object> headers,
        String bodyTemplate,
        UUID defaultRuleId,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static RequestResponse from(Request entity) {
        return new RequestResponse(
                entity.getId(),
                entity.getWorkspace().getId(),
                entity.getFolder() != null ? entity.getFolder().getId() : null,
                entity.getName(),
                entity.getUrl(),
                entity.getMethod(),
                entity.getHeaders(),
                entity.getBodyTemplate(),
                entity.getDefaultRule() != null ? entity.getDefaultRule().getId() : null,
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

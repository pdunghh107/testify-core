package com.zcomini.backend.testify.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.zcomini.backend.testify.entity.Workspace;

/**
 * DTO đại diện cho dữ liệu Không gian làm việc (Workspace) trả về cho Client.
 *
 * @param id          Định danh duy nhất của Workspace.
 * @param name        Tên của Workspace.
 * @param description Mô tả về Workspace.
 * @param createdBy   ID của người dùng đã tạo ra (và sở hữu) Workspace này.
 * @param createdAt   Thời điểm tạo.
 * @param updatedAt   Thời điểm cập nhật gần nhất.
 */
public record WorkspaceResponse(
        UUID id,
        String name,
        String description,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getCreatedBy(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt());
    }
}

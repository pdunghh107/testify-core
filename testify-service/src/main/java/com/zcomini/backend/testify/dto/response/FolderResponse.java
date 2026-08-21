package com.zcomini.backend.testify.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.zcomini.backend.testify.entity.Folder;

public record FolderResponse(
        UUID id,
        UUID workspaceId,
        UUID parentFolderId,
        String name,
        Integer depthLevel,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static FolderResponse from(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getWorkspace() != null ? folder.getWorkspace().getId() : null,
                folder.getParentFolder() != null ? folder.getParentFolder().getId() : null,
                folder.getName(),
                folder.getDepthLevel(),
                folder.getCreatedBy(),
                folder.getCreatedAt(),
                folder.getUpdatedAt());
    }
}

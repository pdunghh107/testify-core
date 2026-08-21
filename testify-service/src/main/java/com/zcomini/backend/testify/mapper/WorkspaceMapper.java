package com.zcomini.backend.testify.mapper;

import java.util.UUID;

import org.springframework.lang.NonNull;

import com.zcomini.backend.testify.dto.request.CreateWorkspaceRequest;
import com.zcomini.backend.testify.entity.Workspace;

public final class WorkspaceMapper {
    private WorkspaceMapper() {
    }

    @NonNull
    public static Workspace toCreate(CreateWorkspaceRequest request, UUID createdBy) {
        Workspace workspace = new Workspace();
        workspace.setName(request.name());
        workspace.setDescription(request.description());
        workspace.setCreatedBy(createdBy);
        return workspace;
    }
}

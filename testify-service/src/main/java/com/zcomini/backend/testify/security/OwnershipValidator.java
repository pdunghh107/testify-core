package com.zcomini.backend.testify.security;

import com.zcomini.backend.testify.entity.Folder;
import com.zcomini.backend.testify.entity.Workspace;
import com.zcomini.backend.testify.exception.TestifyException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OwnershipValidator {

    public void checkOwnership(Workspace workspace, UUID userId) {
        if (workspace.getCreatedBy() == null || !workspace.getCreatedBy().equals(userId)) {
            throw TestifyException.accessDenied();
        }
    }

    public void checkOwnership(Folder folder, UUID userId) {
        if (folder.getCreatedBy() == null || !folder.getCreatedBy().equals(userId)) {
            throw TestifyException.accessDenied();
        }
    }
}

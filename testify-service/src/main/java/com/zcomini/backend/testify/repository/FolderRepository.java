package com.zcomini.backend.testify.repository;

import com.zcomini.backend.testify.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByWorkspaceId(UUID workspaceId);
    
    Optional<Folder> findByIdAndWorkspaceId(UUID id, UUID workspaceId);

    long countByWorkspaceId(UUID workspaceId);

    long countByParentFolderId(UUID parentId);
}

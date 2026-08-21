package com.zcomini.backend.testify.repository;

import com.zcomini.backend.testify.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    long countByWorkspaceId(UUID workspaceId);
    
    List<Request> findByWorkspaceId(UUID workspaceId);

    long countByFolderId(UUID folderId);
}

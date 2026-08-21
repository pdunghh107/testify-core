package com.zcomini.backend.testify.repository;

import com.zcomini.backend.testify.entity.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RuleConfigRepository extends JpaRepository<RuleConfig, UUID> {
    Optional<RuleConfig> findByConfigCode(String configCode);

    boolean existsByConfigCode(String configCode);

    long countByWorkspaceId(UUID workspaceId);
    
    long countByFolderId(UUID folderId);
}

package com.zcomini.backend.testify.repository;

import com.zcomini.backend.testify.entity.FieldConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FieldConfigRepository extends JpaRepository<FieldConfig, UUID> {
    List<FieldConfig> findByWorkspaceId(UUID workspaceId);
}

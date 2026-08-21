package com.zcomini.backend.testify.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcomini.backend.shared.tenant.RequestContext;
import com.zcomini.backend.testify.dto.request.CreateFieldConfigRequest;
import com.zcomini.backend.testify.dto.request.UpdateFieldConfigRequest;
import com.zcomini.backend.testify.dto.response.FieldConfigResponse;
import com.zcomini.backend.testify.entity.FieldConfig;
import com.zcomini.backend.testify.entity.Workspace;
import com.zcomini.backend.testify.exception.FieldConfigException;
import com.zcomini.backend.testify.exception.WorkspaceException;
import com.zcomini.backend.testify.repository.FieldConfigRepository;
import com.zcomini.backend.testify.repository.WorkspaceRepository;
import com.zcomini.backend.testify.security.OwnershipValidator;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FieldConfigService {

    private final FieldConfigRepository fieldConfigRepository;
    private final WorkspaceRepository workspaceRepository;
    private final OwnershipValidator ownershipValidator;

    public FieldConfigResponse createFieldConfig(CreateFieldConfigRequest request) {
        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(WorkspaceException::notFound);

        ownershipValidator.checkOwnership(workspace, RequestContext.getUserId());

        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.setWorkspace(workspace);
        fieldConfig.setName(request.name());
        fieldConfig.setContainsKeywords(request.containsKeywords());
        fieldConfig.setDefaultRegex(request.defaultRegex());
        fieldConfig.setCreatedBy(RequestContext.getUserId());

        fieldConfig = fieldConfigRepository.save(fieldConfig);
        return FieldConfigResponse.from(fieldConfig);
    }

    @Transactional(readOnly = true)
    public List<FieldConfigResponse> getFieldConfigsByWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(WorkspaceException::notFound);

        ownershipValidator.checkOwnership(workspace, RequestContext.getUserId());

        return fieldConfigRepository.findByWorkspaceId(workspaceId).stream()
                .map(FieldConfigResponse::from)
                .collect(Collectors.toList());
    }

    public FieldConfigResponse updateFieldConfig(UUID id, UpdateFieldConfigRequest request) {
        FieldConfig fieldConfig = fieldConfigRepository.findById(id)
                .orElseThrow(FieldConfigException::notFound);

        ownershipValidator.checkOwnership(fieldConfig.getWorkspace(), RequestContext.getUserId());

        fieldConfig.setName(request.name());
        fieldConfig.setContainsKeywords(request.containsKeywords());
        fieldConfig.setDefaultRegex(request.defaultRegex());

        fieldConfig = fieldConfigRepository.save(fieldConfig);
        return FieldConfigResponse.from(fieldConfig);
    }

    public void deleteFieldConfig(UUID id) {
        FieldConfig fieldConfig = fieldConfigRepository.findById(id)
                .orElseThrow(FieldConfigException::notFound);

        ownershipValidator.checkOwnership(fieldConfig.getWorkspace(), RequestContext.getUserId());

        fieldConfigRepository.delete(fieldConfig);
    }
}

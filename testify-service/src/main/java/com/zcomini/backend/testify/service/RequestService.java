package com.zcomini.backend.testify.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcomini.backend.shared.tenant.RequestContext;
import com.zcomini.backend.testify.dto.request.CreateRequestDto;
import com.zcomini.backend.testify.dto.request.UpdateRequestDto;
import com.zcomini.backend.testify.dto.response.RequestResponse;
import com.zcomini.backend.testify.entity.Folder;
import com.zcomini.backend.testify.entity.Request;
import com.zcomini.backend.testify.entity.RuleConfig;
import com.zcomini.backend.testify.entity.Workspace;
import com.zcomini.backend.testify.exception.FolderException;
import com.zcomini.backend.testify.exception.RequestException;
import com.zcomini.backend.testify.exception.RuleConfigException;
import com.zcomini.backend.testify.exception.WorkspaceException;
import com.zcomini.backend.testify.repository.FolderRepository;
import com.zcomini.backend.testify.repository.RequestRepository;
import com.zcomini.backend.testify.repository.RuleConfigRepository;
import com.zcomini.backend.testify.repository.WorkspaceRepository;
import com.zcomini.backend.testify.security.OwnershipValidator;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final RuleConfigRepository ruleConfigRepository;
    private final OwnershipValidator ownershipValidator;

    public RequestResponse createRequest(CreateRequestDto createDto) {
        Workspace workspace = workspaceRepository.findById(createDto.workspaceId())
                .orElseThrow(WorkspaceException::notFound);
        ownershipValidator.checkOwnership(workspace, RequestContext.getUserId());

        Request requestEntity = new Request();
        requestEntity.setWorkspace(workspace);
        requestEntity.setName(createDto.name());
        requestEntity.setUrl(createDto.url());
        requestEntity.setMethod(createDto.method());
        requestEntity.setHeaders(createDto.headers());
        requestEntity.setBodyTemplate(createDto.bodyTemplate());
        requestEntity.setCreatedBy(RequestContext.getUserId());

        if (createDto.folderId() != null) {
            Folder folder = folderRepository.findById(createDto.folderId())
                    .orElseThrow(FolderException::notFound);
            ownershipValidator.checkOwnership(folder.getWorkspace(), RequestContext.getUserId());
            requestEntity.setFolder(folder);
        }

        if (createDto.defaultRuleId() != null) {
            RuleConfig ruleConfig = ruleConfigRepository.findById(createDto.defaultRuleId())
                    .orElseThrow(RuleConfigException::notFound);
            ownershipValidator.checkOwnership(ruleConfig.getWorkspace(), RequestContext.getUserId());
            requestEntity.setDefaultRule(ruleConfig);
        }

        requestEntity = requestRepository.save(requestEntity);
        return RequestResponse.from(requestEntity);
    }

    @Transactional(readOnly = true)
    public List<RequestResponse> getRequestsByWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(WorkspaceException::notFound);
        ownershipValidator.checkOwnership(workspace, RequestContext.getUserId());

        return requestRepository.findByWorkspaceId(workspaceId).stream()
                .map(RequestResponse::from)
                .collect(Collectors.toList());
    }

    public RequestResponse getRequestById(UUID id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(RequestException::notFound);

        Workspace workspace = workspaceRepository.findById(request.getWorkspace().getId())
                .orElseThrow(WorkspaceException::notFound);
        ownershipValidator.checkOwnership(workspace, RequestContext.getUserId());

        return RequestResponse.from(request);
    }

    public RequestResponse updateRequest(UUID id, UpdateRequestDto updateDto) {
        Request requestEntity = requestRepository.findById(id)
                .orElseThrow(RequestException::notFound);
        ownershipValidator.checkOwnership(requestEntity.getWorkspace(), RequestContext.getUserId());

        requestEntity.setName(updateDto.name());
        requestEntity.setUrl(updateDto.url());
        requestEntity.setMethod(updateDto.method());
        requestEntity.setHeaders(updateDto.headers());
        requestEntity.setBodyTemplate(updateDto.bodyTemplate());

        if (updateDto.folderId() != null) {
            Folder folder = folderRepository.findById(updateDto.folderId())
                    .orElseThrow(FolderException::notFound);
            ownershipValidator.checkOwnership(folder.getWorkspace(), RequestContext.getUserId());
            requestEntity.setFolder(folder);
        } else {
            requestEntity.setFolder(null);
        }

        if (updateDto.defaultRuleId() != null) {
            RuleConfig ruleConfig = ruleConfigRepository.findById(updateDto.defaultRuleId())
                    .orElseThrow(RuleConfigException::notFound);
            ownershipValidator.checkOwnership(ruleConfig.getWorkspace(), RequestContext.getUserId());
            requestEntity.setDefaultRule(ruleConfig);
        } else {
            requestEntity.setDefaultRule(null);
        }

        requestEntity = requestRepository.save(requestEntity);
        return RequestResponse.from(requestEntity);
    }

    public void deleteRequest(UUID id) {
        Request requestEntity = requestRepository.findById(id)
                .orElseThrow(RequestException::notFound);
        ownershipValidator.checkOwnership(requestEntity.getWorkspace(), RequestContext.getUserId());
        requestRepository.delete(requestEntity);
    }
}

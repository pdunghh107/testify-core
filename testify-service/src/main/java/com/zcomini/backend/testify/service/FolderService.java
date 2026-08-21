package com.zcomini.backend.testify.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcomini.backend.shared.tenant.RequestContext;
import com.zcomini.backend.testify.dto.request.CreateFolderRequest;
import com.zcomini.backend.testify.dto.response.FolderResponse;
import com.zcomini.backend.testify.entity.Folder;
import com.zcomini.backend.testify.entity.Workspace;
import com.zcomini.backend.testify.exception.FolderException;
import com.zcomini.backend.testify.exception.WorkspaceException;
import com.zcomini.backend.testify.repository.FolderRepository;
import com.zcomini.backend.testify.repository.WorkspaceRepository;
import com.zcomini.backend.testify.repository.RequestRepository;
import com.zcomini.backend.testify.repository.RuleConfigRepository;
import com.zcomini.backend.testify.security.OwnershipValidator;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final WorkspaceRepository workspaceRepository;
    private final RuleConfigRepository ruleConfigRepository;
    private final RequestRepository requestRepository;
    private final OwnershipValidator ownershipValidator;

    public FolderResponse createFolder(UUID workspaceId, CreateFolderRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(WorkspaceException::notFound);

        Folder folder = new Folder();
        folder.setWorkspace(workspace);
        folder.setName(request.name());
        folder.setCreatedBy(RequestContext.getUserId());

        if (request.parentFolderId() != null) {
            Folder parent = folderRepository.findByIdAndWorkspaceId(request.parentFolderId(), workspaceId)
                    .orElseThrow(FolderException::invalidParentFolder);

            if (parent.getDepthLevel() >= 3) {
                throw FolderException.maxDepthExceeded();
            }

            folder.setParentFolder(parent);
            folder.setDepthLevel(parent.getDepthLevel() + 1);
        } else {
            folder.setDepthLevel(1);
        }

        folder = folderRepository.save(folder);
        return FolderResponse.from(folder);
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(WorkspaceException::notFound);
        ownershipValidator.checkOwnership(workspace, RequestContext.getUserId());

        return folderRepository.findByWorkspaceId(workspaceId).stream()
                .map(FolderResponse::from)
                .collect(Collectors.toList());
    }

    public void deleteFolder(UUID id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(FolderException::notFound);

        ownershipValidator.checkOwnership(folder, RequestContext.getUserId());

        long childCount = folderRepository.countByParentFolderId(id);
        long ruleCount = ruleConfigRepository.countByFolderId(id);
        boolean hasRequests = requestRepository.countByFolderId(id) > 0;

        if (childCount > 0 || ruleCount > 0 || hasRequests) {
            throw FolderException.hasData();
        }

        folderRepository.delete(folder);
    }
}

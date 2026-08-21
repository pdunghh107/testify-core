package com.zcomini.backend.testify.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zcomini.backend.shared.api.dto.MessageResponse;
import com.zcomini.backend.testify.dto.request.CreateFolderRequest;
import com.zcomini.backend.testify.dto.response.FolderResponse;
import com.zcomini.backend.testify.service.FolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponse createFolder(@PathVariable UUID workspaceId,
            @Valid @RequestBody CreateFolderRequest request) {
        return folderService.createFolder(workspaceId, request);
    }

    @GetMapping
    public List<FolderResponse> getFoldersByWorkspace(@PathVariable UUID workspaceId) {
        return folderService.getFoldersByWorkspace(workspaceId);
    }

    @DeleteMapping("/{folderId}")
    public MessageResponse deleteFolder(@PathVariable UUID workspaceId, @PathVariable UUID folderId) {
        folderService.deleteFolder(folderId);
        return new MessageResponse("Xóa thư mục thành công.");
    }
}

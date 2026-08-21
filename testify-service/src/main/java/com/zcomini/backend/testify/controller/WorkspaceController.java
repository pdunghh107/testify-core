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
import com.zcomini.backend.testify.dto.request.CreateWorkspaceRequest;
import com.zcomini.backend.testify.dto.response.WorkspaceResponse;
import com.zcomini.backend.testify.service.WorkspaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller cung cấp các API để quản lý Không gian làm việc (Workspace).
 * <p>
 * Workspace là đơn vị phân cấp cao nhất trong hệ thống Testify, giúp cô lập
 * dữ liệu (Data Isolation) giữa các người dùng với nhau.
 */
@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    /**
     * Tạo mới một Workspace.
     *
     * @param request Dữ liệu khởi tạo Workspace (tên, mô tả).
     * @return Thông tin của Workspace vừa được tạo.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkspaceResponse createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        return workspaceService.createWorkspace(request);
    }

    /**
     * Lấy danh sách toàn bộ Workspace thuộc sở hữu của người dùng hiện tại.
     *
     * @return Danh sách các Workspace.
     */
    @GetMapping
    public List<WorkspaceResponse> getAllWorkspaces() {
        return workspaceService.getAllWorkspaces();
    }

    /**
     * Xóa một Workspace dựa trên ID.
     * <p>
     * Chỉ cho phép xóa khi Workspace trống (không chứa Folder, RuleConfig,
     * Request).
     *
     * @param id ID của Workspace cần xóa.
     * @return Thông báo xóa thành công.
     */
    @DeleteMapping("/{id}")
    public MessageResponse deleteWorkspace(@PathVariable UUID id) {
        workspaceService.deleteWorkspace(id);
        return new MessageResponse("Xóa workspace thành công.");
    }
}

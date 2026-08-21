package com.zcomini.backend.testify.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcomini.backend.shared.tenant.RequestContext;
import com.zcomini.backend.testify.dto.request.CreateWorkspaceRequest;
import com.zcomini.backend.testify.dto.response.WorkspaceResponse;
import com.zcomini.backend.testify.entity.Workspace;
import com.zcomini.backend.testify.exception.WorkspaceException;
import com.zcomini.backend.testify.mapper.WorkspaceMapper;
import com.zcomini.backend.testify.repository.FolderRepository;
import com.zcomini.backend.testify.repository.RequestRepository;
import com.zcomini.backend.testify.repository.RuleConfigRepository;
import com.zcomini.backend.testify.repository.WorkspaceRepository;
import com.zcomini.backend.testify.security.OwnershipValidator;

import lombok.RequiredArgsConstructor;

/**
 * Service xử lý các nghiệp vụ liên quan đến Không gian làm việc (Workspace).
 * <p>
 * Đảm bảo các quy tắc nghiệp vụ như: Data Isolation (cô lập dữ liệu theo user),
 * kiểm tra quyền sở hữu (Ownership validation) trước khi cho phép thao tác (sửa, xóa).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final RuleConfigRepository ruleConfigRepository;
    private final RequestRepository requestRepository;
    private final OwnershipValidator ownershipValidator;

    /**
     * Tạo mới một Workspace cho người dùng hiện hành.
     *
     * @param request Dữ liệu đầu vào (tên, mô tả).
     * @return Dữ liệu Workspace sau khi lưu thành công.
     */
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        Workspace workspace = WorkspaceMapper.toCreate(request, RequestContext.getUserId());
        workspace = workspaceRepository.save(workspace);
        return WorkspaceResponse.from(workspace);
    }

    /**
     * Lấy danh sách toàn bộ Workspace thuộc về người dùng đang thực hiện request.
     * <p>
     * ID của người dùng được lấy tự động từ {@link RequestContext}.
     *
     * @return Danh sách Workspace.
     */
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getAllWorkspaces() {
        return workspaceRepository.findByCreatedBy(RequestContext.getUserId()).stream().map(WorkspaceResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra xem một người dùng đã có ít nhất một Workspace nào chưa.
     *
     * @param userId ID của người dùng.
     * @return {@code true} nếu đã có Workspace, ngược lại {@code false}.
     */
    @Transactional(readOnly = true)
    public boolean hasWorkspace(UUID userId) {
        return workspaceRepository.countByCreatedBy(userId) > 0;
    }

    /**
     * Xóa một Workspace khỏi hệ thống.
     * <p>
     * Thao tác này sẽ bị chặn nếu:
     * 1. Workspace không thuộc quyền sở hữu của người dùng hiện tại (lỗi 403).
     * 2. Workspace đang chứa dữ liệu con (Folder, RuleConfig, Request) (lỗi 400).
     *
     * @param id ID của Workspace cần xóa.
     * @throws WorkspaceException Nếu không tìm thấy, không có quyền xóa hoặc Workspace không trống.
     */
    public void deleteWorkspace(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(WorkspaceException::notFound);

        ownershipValidator.checkOwnership(workspace, RequestContext.getUserId());

        long folderCount = folderRepository.countByWorkspaceId(id);
        long ruleCount = ruleConfigRepository.countByWorkspaceId(id);
        boolean hasRequests = requestRepository.countByWorkspaceId(id) > 0;

        if (folderCount > 0 || ruleCount > 0 || hasRequests) {
            throw WorkspaceException.hasData();
        }

        workspaceRepository.delete(workspace);
    }

}

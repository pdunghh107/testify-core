package com.zcomini.backend.testify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO đại diện cho yêu cầu tạo mới Không gian làm việc (Workspace).
 *
 * @param name        Tên của Workspace, bắt buộc nhập và có độ dài từ 2 đến 50 ký tự.
 * @param description Mô tả chi tiết về Workspace (không bắt buộc, tối đa 1000 ký tự).
 */
public record CreateWorkspaceRequest(
        @NotBlank(message = "{workspace.name.required}") @Size(min = 2, max = 50, message = "{workspace.name.invalid}") String name,
        @Size(max = 1000, message = "{workspace.description.invalid}") String description) {
}

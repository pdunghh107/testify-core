# Goal Description
Đổi toàn bộ thuật ngữ "Project" thành "Workspace" trên toàn hệ thống (Cả UI lẫn Core Backend) để phù hợp hơn với mô hình của ứng dụng Testify.

## Scope of Changes

### 1. Database (PostgreSQL via Flyway)
- Cần tạo một file migration mới (VD: `V2__Rename_project_to_workspace.sql`) trong `testify-service` hoặc sửa trực tiếp file `V1` để:
  - Đổi tên table `projects` -> `workspaces`.
  - Đổi tên các foreign key column `project_id` -> `workspace_id` trong các bảng (VD: `folders`).
  - Đổi tên các index/constraint liên quan.

### 2. Backend (`testify_core/testify-service`)
- **Entities**: Đổi tên class `Project.java` -> `Workspace.java`, cập nhật `@Table(name="workspaces")`.
- **Repositories**: Đổi `ProjectRepository.java` -> `WorkspaceRepository.java`.
- **Services**: `ProjectService.java` -> `WorkspaceService.java`.
- **Controllers**: `ProjectController.java` -> `WorkspaceController.java`, cập nhật endpoint URL từ `/api/v1/projects` -> `/api/v1/workspaces`.
- **DTOs**: `CreateProjectRequest` -> `CreateWorkspaceRequest`, `ProjectResponse` -> `WorkspaceResponse`.
- **Exceptions**: `ProjectException` -> `WorkspaceException`.
- **Security**: Cập nhật method `checkOwnership(Project project, UUID userId)` thành `checkOwnership(Workspace workspace, UUID userId)`.
- **Mappers**: `ProjectMapper` -> `WorkspaceMapper`.

### 3. Frontend (`testify_ui`)
- **Store Zustand**: Đổi tên file `projectStore.ts` -> `workspaceStore.ts`. Đổi các biến state `activeProjectId`, `setActiveProject` thành `activeWorkspaceId`, `setActiveWorkspace`.
- **API Queries**: Đổi file `useProjectQueries.ts` -> `useWorkspaceQueries.ts`. Thay đổi endpoint gọi API sang `/api/v1/workspaces`.
- **Components/UI**:
  - `ProjectSelector.tsx` -> `WorkspaceSelector.tsx`
  - Các Modals: `CreateProjectModal.tsx` -> `CreateWorkspaceModal.tsx`, `DeleteProjectModal` -> `DeleteWorkspaceModal`.
  - Text hiển thị trên UI: Đổi các label "Tạo dự án", "Chọn dự án" thành "Tạo Workspace", "Chọn Workspace".
- **Types**: Đổi interface `Project` thành `Workspace`.
- **Hooks**: Cập nhật logic trong `useWorkspaceTree.tsx` để lấy `activeWorkspaceId` thay vì Project.

## User Review Required
> [!WARNING]
> Đây là một thay đổi lớn (Major Refactor), tác động chéo lên toàn bộ hệ thống (Database, API, Frontend). Code sẽ gặp lỗi biên dịch trong quá trình thực hiện cho đến khi hoàn tất.

## Open Questions
> [!IMPORTANT]
> 1. Về Database (Flyway): Dự án của bạn đã deploy (Go-live) chưa? Mình nên tạo một file migration mới (`V2__rename_to_workspace.sql`) hay là mình được phép sửa trực tiếp file khởi tạo `V1__init.sql` (Sửa V1 sẽ sạch sẽ DB hơn nhưng bạn sẽ cần xoá DB local và chạy lại từ đầu)?
> 2. Có text nào trên UI bạn muốn việt hoá "Workspace" thành "Không gian làm việc" không, hay cứ giữ nguyên chữ tiếng Anh là "Workspace"?

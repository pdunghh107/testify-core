# Todo: Rename Project to Workspace

- [ ] **Giai đoạn 1: Database & Backend (`testify_core`)**
  - [ ] Cập nhật/Tạo file Flyway Migration (Rename table/columns/constraints).
  - [ ] Đổi tên `Project.java` -> `Workspace.java` và chỉnh sửa các field liên quan trong `Folder.java`.
  - [ ] Đổi tên `ProjectRepository` -> `WorkspaceRepository`.
  - [ ] Đổi tên các class DTOs (`CreateProjectRequest`, `ProjectResponse`,...) và Mappers.
  - [ ] Đổi tên `ProjectService` -> `WorkspaceService` & `OwnershipValidator`.
  - [ ] Đổi tên `ProjectController` -> `WorkspaceController` và sửa endpoint `/api/v1/projects` thành `/api/v1/workspaces`.
  - [ ] Đổi tên custom exception `ProjectException` -> `WorkspaceException`.
  - [ ] Build lại Backend để đảm bảo không còn lỗi syntax/import.

- [ ] **Giai đoạn 2: Frontend (`testify_ui`)**
  - [ ] Đổi tên Interface Data Type `Project` -> `Workspace`.
  - [ ] Đổi tên file Zustand Store `projectStore.ts` -> `workspaceStore.ts` (đổi `activeProjectId` -> `activeWorkspaceId`).
  - [ ] Đổi tên React Query file `useProjectQueries.ts` -> `useWorkspaceQueries.ts` (cập nhật Endpoint API).
  - [ ] Đổi tên các Components: `ProjectSelector` -> `WorkspaceSelector`, `CreateProjectModal` -> `CreateWorkspaceModal`, v.v.
  - [ ] Cập nhật các hook đang sử dụng (như `useWorkspaceTree.tsx`) và layout `Header.tsx`.
  - [ ] Cập nhật toàn bộ text Label trên UI.

- [ ] **Giai đoạn 3: Kiểm thử & Nghiệm thu**
  - [ ] Khởi động lại Backend và xoá DB cũ (nếu sửa file V1) để chạy lại Flyway.
  - [ ] Test luồng tạo Workspace mới, hiển thị trên Header, và load các Folder bên trong Workspace đó.

# Danh sách công việc (Todo List)

- [x] **Bước 1: Cập nhật JwtService tạo JTI**
  - Thêm `.id(UUID.randomUUID().toString())` vào hàm `createAccessToken()` để sinh chuỗi `jti` duy nhất cho mỗi Access Token.

- [x] **Bước 2: Cập nhật JwtAuthenticationFilter**
  - Đọc claim `jti` từ Token (thông qua `claims.getId()`).
  - Query Redis xem có tồn tại key `token_revoked:{jti}` hay không.
  - Nếu tồn tại, ném ra ngoại lệ `AuthException.tokenRevoked()` để chặn request.

- [x] **Bước 3: Cập nhật AuthService - Case 1 (Logout 1 thiết bị)**
  - Chỉnh sửa logic của API `/logout`. Lấy ra chuỗi JWT Access Token (từ Header) hoặc truyền thêm `jti` và `expiration` xuống từ Filter.
  - Tính toán thời gian còn lại của Access Token: `TTL = exp - now`.
  - Lưu key `token_revoked:{jti}` vào Redis với TTL vừa tính toán được.
  - Thu hồi (Revoke) Refresh Token trong Database.

- [x] **Bước 4: Thêm API cho Case 2 (Logout tất cả thiết bị)**
  - Tạo endpoint mới trong `AuthController`: `POST /api/v1/auth/logout-all`.
  - Tạo hàm `logoutAll(AuthenticatedUser principal)` trong `AuthService`.
  - Ghi đè Redis với key `user_revoked:{userId}` theo thời gian hiện tại (sử dụng lại logic từ hàm `deactivateAccount`).
  - Gọi Repository để update `revokedAt = now()` cho toàn bộ Refresh Token đang active của user.
  - Xóa Refresh Token Cookie trên response.
  
- [ ] **(Mở rộng) Bước 5: Lưu thông tin thiết bị lúc Login**
  - Thêm cột `device_info` (hoặc `user_agent`) vào Entity `RefreshTokenEntity`.
  - Tại hàm login/register, lấy Header `User-Agent` truyền xuống `AuthService`.
  - Lưu `User-Agent` khi tạo Refresh Token để sau này có thể làm API quản lý lịch sử đăng nhập/thiết bị hoạt động.

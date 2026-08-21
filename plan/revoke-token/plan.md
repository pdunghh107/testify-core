# Kế hoạch triển khai Redis Blacklist cho Access Token

## 1. Phân tích bài toán thiết bị (Device Identification)
Không bắt buộc phải dùng Firebase để nhận diện thiết bị. Cách thực hiện chuẩn xác nhất trong mô hình Stateless (JWT) hiện tại:
- **Nhận diện thiết bị (Session):** Mỗi lần đăng nhập, một `RefreshTokenEntity` được tạo ra. Đây chính là đại diện cho 1 "thiết bị" hoặc 1 "phiên đăng nhập". Để hiển thị tên thiết bị (VD: "Chrome trên Windows", "iPhone 15"), ta có thể lấy từ header `User-Agent` của HTTP request lúc gọi API `/login` và lưu vào bảng `refresh_tokens`.
- **Nhận diện Access Token (JTI):** Hiện tại Access Token của dự án chưa có ID định danh. Ta cần bổ sung claim `jti` (JWT ID) bằng cách gen một UUID. Nhờ có `jti`, ta có thể chặn chính xác 1 Access Token cụ thể trên Redis.

## 2. Thiết kế giải pháp cho 2 luồng (Cases)

### Case 1: Logout 1 thiết bị (Thiết bị/phiên hiện tại)
**Mục tiêu:** Chỉ hủy phiên đăng nhập hiện tại, các thiết bị khác (nếu có) vẫn hoạt động bình thường.
- **Bước 1:** Trích xuất `jti` (JWT ID) và thời gian hết hạn (`exp`) từ Access Token hiện tại của user.
- **Bước 2:** Lưu `jti` này vào Redis với key `token_revoked:{jti}`, thời gian sống (TTL) trên Redis bằng đúng thời gian còn lại của Access Token (tránh rác Redis).
- **Bước 3:** Đánh dấu `revokedAt = NOW()` cho Refresh Token (gửi qua Cookie) trong Database (như code hàm `logout` hiện tại đang làm).
- **Bước 4:** Cập nhật `JwtAuthenticationFilter`: Tại mỗi request, ngoài việc check user bị revoke, còn phải lấy `jti` ra kiểm tra xem Redis có key `token_revoked:{jti}` hay không. Nếu có -> Chặn.

### Case 2: Logout tất cả thiết bị (Global Logout)
**Mục tiêu:** Kích hoạt tính năng đăng xuất trên mọi thiết bị, toàn bộ token của User trên mọi máy tính/điện thoại sẽ bị hủy.
- **Bước 1:** Đưa `userId` vào Redis với key `user_revoked:{userId}` kèm theo mốc thời gian hiện tại (Tận dụng lại đúng logic của API `deactivateAccount`).
- **Bước 2:** Quét DB và cập nhật `revokedAt = NOW()` cho **TẤT CẢ** các bản ghi `refresh_tokens` của `userId` này (những bản ghi chưa bị revoke).
- **Bước 3:** Hàm `JwtAuthenticationFilter` (đã có sẵn logic này) sẽ kiểm tra thời gian `issuedAt` của mọi Access Token so với mốc thời gian `user_revoked:{userId}` trong Redis. Nếu token được tạo trước đó -> Lập tức từ chối.

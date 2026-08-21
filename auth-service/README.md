# Auth Service (Dịch vụ Xác thực & Phân quyền)

`auth-service` là một module độc lập thuộc hệ thống Testify Core, đảm nhận toàn bộ nghiệp vụ liên quan đến quản lý tài khoản người dùng, xác thực (Authentication) và phân quyền (Authorization).

## 🏗️ Cấu trúc dự án

Dự án tuân theo kiến trúc MVC chuẩn của Spring Boot, kết hợp với các nguyên tắc SOLID:
- `controller`: Chứa các REST API endpoint để giao tiếp với Client.
- `service`: Chứa logic nghiệp vụ lõi (Business Logic).
- `repository`: Giao tiếp trực tiếp với cơ sở dữ liệu qua Spring Data JPA.
- `entity`: Định nghĩa các thực thể (Entity) ánh xạ với các bảng trong cơ sở dữ liệu.
- `dto`: Các đối tượng Data Transfer Object (Request/Response), ưu tiên sử dụng `record` (Java 14+) để tăng tính bất biến (Immutability).
- `config`: Cấu hình toàn bộ hệ thống, bao gồm cấu hình Spring Security (`SecurityConfig`), RabbitMQ (`AuditLogRabbitConfig`, `NotificationRabbitConfig`), S3/MinIO, Swagger API Docs, và cấu hình phân giải Properties.
- `security`: Chứa bộ lọc bảo mật (`JwtAuthenticationFilter`) và mô hình đối tượng đại diện cho người dùng đã đăng nhập (`AuthenticatedUser`).
- `client`: Đảm nhận việc giao tiếp với các External Service hoặc Host App thông qua HTTP Client (như FeignClient / RestClient).
- `exception`: Quản lý các ngoại lệ (Exception) nội bộ phát sinh riêng của auth-service.
- `mapper`: Xử lý chuyển đổi qua lại giữa các đối tượng Entity và DTO.

## 🛠️ Công nghệ sử dụng (Tech Stack)

- **Ngôn ngữ & Framework:** Java 21, Spring Boot 3.4.1.
- **Bảo mật:** Spring Security + JSON Web Token (JWT).
- **Cơ sở dữ liệu:** PostgreSQL (Lưu trữ chính) & Flyway (Database Migration).
- **Lưu trữ file:** MinIO (Dùng để lưu trữ Avatar).
- **Message Broker:** RabbitMQ (Giao tiếp bất đồng bộ).
- **Cache:** Redis (Quản lý Token Blacklist).
- **Khác:** Lombok, MapStruct (tùy chọn), Validation.

## 🔌 Các API được cung cấp (Endpoints)

Dưới đây là danh sách các API chính mà `auth-service` (đang chạy mặc định ở port `9001`) cung cấp.

### 1. Xác thực (Authentication)
- `POST /api/v1/auth/register`: Đăng ký tài khoản người dùng mới.
- `POST /api/v1/auth/login`: Đăng nhập, nhận Access Token để xác thực cho các request tiếp theo.
- `POST /api/v1/auth/refresh`: Refresh token.

### 2. Quản lý tài khoản (Profile / Me)
- `GET /api/v1/auth/me`: Lấy thông tin cá nhân của người dùng hiện tại (yêu cầu Access Token).
- `PUT /api/v1/auth/me`: Cập nhật thông tin hồ sơ người dùng (Họ tên, SĐT, đường dẫn Avatar).
- `POST /api/v1/auth/me/password`: Thay đổi mật khẩu (yêu cầu nhập đúng mật khẩu cũ).
- `POST /api/v1/auth/me/deactivate`: Vô hiệu hoá tài khoản (yêu cầu nhập đúng mật khẩu hiện tại).

## 📝 Tài liệu API (Postman Collection)

Toàn bộ các Request mẫu, cấu trúc Body Payload, Headers và các đoạn script tự động (tự động lấy và lưu Token) đã được thiết lập sẵn trong Postman Collection chung của cả dự án Testify.

👉 **[Nhấn vào đây để xem Testify Postman Collection](../testify.postman_collection.json)** *(Đường dẫn tương đối trỏ ra thư mục gốc `testify_core`)*

> **💡 Mẹo sử dụng Postman:** 
> Collection đã được cấu hình các Script ở mục `Tests`. Sau khi bạn gọi API **Login** hoặc **Register** thành công, hệ thống sẽ tự động bắt lấy `accessToken` và lưu vào biến `{{ACCESS_TOKEN}}`. Khi đó, bạn có thể gọi ngay các API yêu cầu quyền đăng nhập (như `/me`) mà không cần phải copy/paste token thủ công!

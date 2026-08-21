# Testify Core (Backend)

Testify Core là hệ thống backend cho ứng dụng Testify (Nền tảng quản lý và chạy test API tương tự Postman). Dự án được thiết kế theo dạng Modular Monolith / Microservices để dễ dàng mở rộng, quản lý và tái sử dụng mã nguồn.

## 🏗️ Cấu trúc dự án (Modules)

Hệ thống bao gồm 3 module chính:

### 1. `shared-kernel` (Thư viện dùng chung)
Đây là module cốt lõi chứa các thành phần, tiện ích và cấu hình dùng chung cho toàn bộ hệ thống. Các service khác đều phụ thuộc (phải khai báo dependency) vào module này để tránh lặp lại code (Nguyên tắc DRY).
- **Tính năng chính:**
  - `api`: Cấu trúc chuẩn hóa cho API Response chung.
  - `exception`: Global Exception Handling (Bắt và xử lý lỗi đồng nhất).
  - `validation`: Các annotation và rule validation tùy biến.
  - `tenant/context`: Xử lý Data Isolation (`RequestContext`, `UserId`).
  - `util`: Các hàm tiện ích (thao tác với JWT, Date, String).
- **Công nghệ/Thư viện:** Spring Web, Spring Data JPA, JWT, Lombok.

### 2. `auth-service` (Dịch vụ Xác thực & Phân quyền)
Service độc lập đảm nhận toàn bộ nghiệp vụ liên quan đến tài khoản người dùng, xác thực (Authentication) và phân quyền (Authorization).
- **Tính năng chính:**
  - Quản lý User (Đăng nhập, Đăng ký, Quản lý Profile).
  - Cấu hình Spring Security.
  - Cấp phát và xác minh JWT Token.
  - Tương tác với MinIO để lưu trữ và quản lý file (Avatar/Tài liệu).
- **Công nghệ/Thư viện:** Spring Security, Spring Web, Spring Data JPA, PostgreSQL, Flyway, JWT, MinIO, Apache Tika, RabbitMQ (AMQP).

### 3. `testify-service` (Dịch vụ Nghiệp vụ Lõi)
Service xử lý logic cốt lõi của ứng dụng Testify: Quản lý và thực thi (run) các bài kiểm thử API.
- **Tính năng chính:**
  - **Quản lý tài nguyên:** Phân cấp dữ liệu theo `Project` -> `Folder` -> `Request`.
  - **Quản lý cấu hình Test:** Lưu trữ các rules qua `TestRule`, `RuleConfig`, `FieldConfig`.
  - **Engine:** Chứa hệ thống Rule Runner để phân tích, giả lập và đánh giá các API request.
  - **Data Isolation:** Đảm bảo mỗi User chỉ có thể quản lý và chạy test trên không gian làm việc của chính họ.
- **Công nghệ/Thư viện:** Spring Web, Spring Data JPA, PostgreSQL, Flyway, Jackson, Datafaker (tạo dữ liệu giả).

---

## 🛠️ Công nghệ sử dụng
- **Nền tảng:** Java 21, Spring Boot 3.4.1.
- **Cơ sở dữ liệu:** PostgreSQL, H2 (Test).
- **Database Migration:** Flyway.
- **Bảo mật:** Spring Security + JWT.
- **Lưu trữ đối tượng:** MinIO.
- **Message Broker:** RabbitMQ.
- **Cache / In-memory Data:** Redis (Quản lý Token Blacklist).
- **Khác:** Lombok, Datafaker.

### 🔮 Công nghệ đề xuất khi mở rộng dự án (Recommended Tech Stack)
Khi hệ thống phát triển lớn hơn, đây là những công nghệ chuẩn Enterprise được đề xuất để tích hợp thêm:
1. **API Gateway & Service Discovery:** Spring Cloud Gateway, Netflix Eureka (Định tuyến tập trung, Load Balancing).
2. **Centralized Logging & Monitoring:** ELK Stack (Elasticsearch, Logstash, Kibana) hoặc Grafana + Prometheus + Loki để theo dõi Metrics và Log tập trung của tất cả các service.
3. **CI/CD:** GitHub Actions hoặc GitLab CI kết hợp ArgoCD để tự động hoá test và deploy lên nền tảng Kubernetes.
4. **Distributed Tracing:** Micrometer + Zipkin / Jaeger (Cực kỳ quan trọng để theo dõi một Request chạy xuyên qua `auth-service` sang `testify-service` mất bao nhiêu mili-giây).
5. **Caching Layer mở rộng:** Sử dụng Redis Cluster để cache Data (ví dụ cache danh sách Workspace) thay vì chỉ dùng cho Token Blacklist như hiện tại.

---

## 📊 Giám sát Hệ thống (Monitoring)
Hệ thống đã được tích hợp sẵn **Prometheus** và **Grafana** qua Docker Compose để theo dõi sức khỏe và hiệu suất.

- **Prometheus:** `http://localhost:9090`
- **Grafana:** `http://localhost:3000` (Tài khoản mặc định: `admin`/`admin`)

### Cách khởi động Monitoring:
1. Mở terminal tại thư mục `testify_core`.
2. Chạy lệnh: `docker-compose up -d prometheus grafana`
3. Chạy các service (`auth-service`, `testify-service`) trên IntelliJ.
4. Truy cập Grafana, thêm Data Source là Prometheus (URL: `http://prometheus:9090`) và import Dashboard (VD: ID 4701).

> **LƯU Ý QUAN TRỌNG VỀ NETWORK:**
> Cấu hình mặc định trong file `prometheus/prometheus.yml` đang sử dụng `host.docker.internal` để lấy (scrape) dữ liệu. Điều này có nghĩa là nó giả định **bạn đang chạy 2 service `auth-service` và `testify-service` trực tiếp bằng IDE (IntelliJ/Eclipse) trên máy thật**.
> 
> Nếu sau này bạn deploy (chạy) 2 service này hoàn toàn bên trong Docker, bạn cần vào file `prometheus/prometheus.yml` và đổi:
> - `host.docker.internal:8081` -> `auth-service:8081` (hoặc IP docker cấp)
> - `host.docker.internal:9002` -> `testify-service:9002`

---

## 📜 Tiêu chuẩn Lập trình (Coding Convention)
Vui lòng tham khảo các file hướng dẫn trong thư mục `.agents` và `skill/`:
1. **RESTful API:** Các endpoint luôn dùng danh từ số nhiều (Ví dụ: `/api/v1/projects`).
2. **DTOs:** Ưu tiên sử dụng `record` (tính năng của Java 14+) để viết DTO ngắn gọn và Immutability.
3. **Bảo mật & IDOR:** Bắt buộc áp dụng Data Isolation. Các API thao tác trên tài nguyên phải được kiểm tra quyền sở hữu bằng `OwnershipValidator.checkOwnership()`.
4. **Validation:** Tuân thủ nghiêm ngặt theo `spring-validation-rules.md` cho các Request DTO.
5. **Javadoc:** Bắt buộc tuân thủ quy chuẩn viết Javadoc. Xem chi tiết tại `skill/writing-javadoc/SKILL.md`.

---

## 📖 Tài liệu API (Swagger UI & Javadoc)

Toàn bộ thông tin hướng dẫn truy cập giao diện API tương tác (Swagger UI) và mã nguồn tĩnh (Javadoc) của các module trong dự án đã được tổng hợp chi tiết tại một file riêng biệt.

👉 **[Xem toàn bộ danh sách đường dẫn tại đây](./API_DOCS.md)**

---

## 📚 Learner (Tài liệu học tập & Phân tích hệ thống)
Dự án có đi kèm một thư mục `learner/` chứa các tài liệu phân tích kỹ thuật chuyên sâu về cách hệ thống Testify hoạt động dưới mui xe (*under the hood*). 

👉 Xem chi tiết danh sách tài liệu tại: **[Thư mục Learner](./learner/README.md)**.

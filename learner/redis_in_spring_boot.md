# Tổng quan về Redis trong kiến trúc hệ thống lớn (Java Spring Boot)

## 1. Redis là gì?

Redis (Remote Dictionary Server) là một hệ quản trị cơ sở dữ liệu NoSQL lưu trữ dữ liệu hoàn toàn trên RAM (In-memory Data Structure Store). Vì đọc/ghi trực tiếp trên RAM nên tốc độ của Redis cực kỳ nhanh (thường tính bằng phần nghìn giây - microsecond). Nó hỗ trợ nhiều cấu trúc dữ liệu phong phú như Strings, Hashes, Lists, Sets, Sorted Sets...

## 2. Vai trò của Redis trong các dự án Enterprise (Spring Boot)

Trong các hệ thống lớn, cơ sở dữ liệu quan hệ (PostgreSQL, MySQL) thường là điểm "thắt cổ chai" (bottleneck) vì phải đọc/ghi xuống ổ cứng vật lý (Disk I/O). Do đó, Redis được kẹp vào kiến trúc để giải quyết các bài toán sau:

### a) Distributed Caching (Bộ nhớ đệm phân tán)

Thay vì mỗi lần có request gửi lên, Backend phải chui xuống Database để query lại từ đầu (rất chậm), Backend sẽ query DB lần đầu và ném kết quả đó vào Redis. Lần sau có request tương tự, Backend chỉ cần móc từ Redis ra trả về ngay lập tức. (Công cụ thường dùng: `@Cacheable`, `@CachePut` trong Spring Cache).

### b) Token Blacklist / Session Storage (Quản lý phiên đăng nhập)

Trong kiến trúc Microservices / Stateless (như dùng JWT), Server không lưu trạng thái đăng nhập. Nhưng khi User bấm "Đăng xuất" hoặc "Bị khóa tài khoản", ta phải vô hiệu hóa JWT đó. Redis rất phù hợp để lưu danh sách các Token bị cấm (Blacklist) vì tốc độ tra cứu siêu tốc.

### c) Rate Limiting (Giới hạn truy cập)

Để chống Spam / DDoS API, người ta lưu IP của user vào Redis và dùng các thuật toán (như Token Bucket) để đếm số lần gọi API trong 1 khoảng thời gian. Nếu vượt quá, hệ thống sẽ chặn lại.

### d) Distributed Lock (Khóa phân tán)

Khi chạy nhiều con Server Spring Boot song song (Cluster), nếu có 2 user cùng thao tác vào 1 tài nguyên, có thể gây lỗi Race Condition. Redis cung cấp cơ chế Lock (thường kết hợp thư viện `Redisson`) để khóa dữ liệu lại, đảm bảo tại 1 thời điểm chỉ 1 con Server được phép can thiệp.

---

## 3. Redis đang được sử dụng như thế nào trong Testify Core?

Hiện tại, sau khi phân tích mã nguồn, Redis đang được dùng **duy nhất ở module `auth-service`**.

### a) Chức năng: Quản lý Token Blacklist

Khi một User bị khóa tài khoản (Deactivate API), hệ thống bắt buộc phải vô hiệu hóa Access Token (JWT) của người đó ngay lập tức, ngăn không cho họ tiếp tục gọi API.

### b) Chi tiết luồng code:

1. **Lưu Key vào Redis:** Tại class `AuthService.java` (khi vô hiệu hóa), hệ thống dùng `StringRedisTemplate` để đẩy user vào Blacklist:
   ```java
   String blacklistKey = "user_revoked:" + user.getId();
   stringRedisTemplate.opsForValue().set(blacklistKey, String.valueOf(Instant.now().toEpochMilli()), 30, TimeUnit.MINUTES);
   ```
2. **Chặn Request:** Tại class `JwtAuthenticationFilter.java` (đứng gác ở cửa), mỗi khi có request gửi kèm Token, hệ thống sẽ chọc vào Redis để check:
   ```java
   String blacklistKey = "user_revoked:" + subject; // subject là userId lấy từ Token
   String revokedTimestampStr = stringRedisTemplate.opsForValue().get(blacklistKey);
   // Nếu Key tồn tại => Ném lỗi AuthException.tokenRevoked() chặn truy cập!
   ```

### c) Các Key hiện có trong hệ thống

Cấu trúc Key duy nhất được lưu trong Redis lúc này là:

- **Định dạng Key:** `user_revoked:{userId}` (Ví dụ: `user_revoked:ebc123...`)
- **Value (Giá trị):** Timestamp (thời điểm tài khoản bị khóa tính bằng mili-giây).
- **Time To Live (TTL):** 30 phút. (Khớp với thời gian sống của Access Token, sau 30 phút token thực cũng tự chết nên Redis sẽ tự động xóa Key này đi để dọn dẹp RAM).

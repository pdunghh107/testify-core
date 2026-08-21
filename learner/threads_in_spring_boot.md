# Phân tích Threads trong dự án Testify (Java 21 + Spring Boot 3.4)

Dự án Testify hiện tại đang sử dụng **Java 21** và đã kích hoạt tính năng **Virtual Threads** thông qua cấu hình `spring.threads.virtual.enabled: true` trong file `application.yml`.

Do đó, mô hình Threads của hệ thống được chia làm hai loại rõ rệt như sau:

## 1. Threads Ảo (Virtual Threads)

Trong mô hình Spring Boot truyền thống, Tomcat sử dụng một Thread Pool với tối đa 200 **Platform Threads** để xử lý request. Tuy nhiên, nhờ cấu hình bật Virtual Thread, Tomcat sẽ chuyển hoàn toàn sang sử dụng **Virtual Threads** cho việc tiếp nhận HTTP Request.

- **Nhiệm vụ:** Xử lý **tất cả** các luồng gọi API (HTTP Requests) từ người dùng (Frontend/Postman) đâm vào Controller.
- **Đặc điểm:** Cứ mỗi một request đến, Tomcat sẽ sinh ra một Virtual Thread mới thay vì lấy từ Pool. Khi request phải chờ đợi (ví dụ: query Database, gọi mạng tới MinIO, gọi HTTP call), Virtual Thread sẽ lập tức "nhường" (unmount) Carrier Thread (Thread thực bên dưới) cho request khác dùng.
- **Số lượng:** Có thể tồn tại đồng thời **hàng triệu** Virtual Threads mà không lo cạn kiệt RAM (mỗi VT chỉ tốn vài trăm Bytes/vài KB bộ nhớ thay vì 1-2MB như Thread thực).

### 💡 Áp dụng thực tế trong dự án Testify:

Cụ thể, khi một request bay vào hệ thống, nó sẽ được cõng bởi 1 **Thread Ảo**:

- **AuthController:** Khi người dùng gọi API `/api/v1/auth/login`, `/register`, `/me/deactivate`.
- **WorkspaceController / FolderController / v.v:** Mọi API gọi vào `testify-service`.
- **Luồng xử lý:** Khi Thread Ảo chạy vào Service, gọi xuống `UserRepository` hay `WorkspaceRepository` để lưu DB -> Nó gặp thao tác I/O blocking (phải chờ DB trả lời) -> Thread Ảo này lập tức _unmount_ để nhường Carrier Thread cho request khác, không gây tắc nghẽn.

## 2. Threads Thực (Platform Threads / OS Threads)

Mặc dù request HTTP dùng Virtual Threads, nhưng hệ thống vẫn duy trì một lượng **Threads thực (Platform Threads)** nhất định để duy trì phần lõi của JVM và các cơ chế nền.

Các Threads thực mang tính khái niệm chung đang chạy ngầm bao gồm:

1. **JVM Threads:**
   - Garbage Collector (GC Threads): Dọn dẹp rác bộ nhớ.
   - JIT Compiler Threads: Biên dịch bytecode sang mã máy.
   - Signal Dispatcher.
2. **Main Thread:** Chạy hàm `main()` khởi động ứng dụng Spring Boot.
3. **HikariCP (Database Connection Pool):** Các thread thực duy trì kết nối vật lý (TCP socket) tới PostgreSQL. Mặc định là khoảng 10 connections (tương đương 10 threads ngầm).
4. **RabbitMQ Listener Threads:** Các thread lắng nghe tin nhắn từ RabbitMQ. (Mặc định Spring AMQP dùng Platform threads cho Message Listener trừ khi bạn chủ động cấu hình TaskExecutor cho nó chuyển sang dùng Virtual Threads).
5. **Carrier Threads (ForkJoinPool):** Đây là các "Thread công nhân" (Platform Threads) nằm bên dưới dùng để cõng (mount) các Virtual Threads. Số lượng Carrier Threads thường mặc định bằng đúng số lượng nhân (CPU cores) của máy chủ vật lý.

### 💡 Áp dụng thực tế trong dự án Testify:

Bên cạnh các cơ chế mặc định của Spring, trong dự án Testify chúng ta thấy rõ sự xuất hiện của các luồng thực sau:

- **RabbitMQ Consumer (`UserRegisteredEventListener`):** Luồng đang lắng nghe sự kiện tạo Workspace ở `testify-service` đang chạy dưới dạng Thread thực.
- **Lettuce / Netty (Kết nối Redis ở `auth-service`):** Thư viện kết nối Redis dùng Netty (Event Loop). Đây là các I/O Threads thực cực kỳ tối ưu của hệ điều hành.
- **Tomcat Acceptor/Poller:** Luồng đứng ở cửa "đón khách" (nhận TCP Connection từ Postman/Browser gửi tới port 9001/9002) luôn là Thread Thực.

## 🎇 Tổng kết

Cứ cái gì phục vụ trực tiếp cho **API HTTP (REST)** thì đang là **Ảo**. Còn những cái làm **cơ sở hạ tầng** (lắng nghe event, duy trì mạng, dọn rác, pool DB) thì đang là **Thực**. Sự kết hợp này mang lại hiệu suất đỉnh cao nhất cho Java 21!

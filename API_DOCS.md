# 📚 Tài liệu API & Javadoc

Tài liệu này tổng hợp tất cả các đường dẫn để truy cập **Javadoc** và **Swagger UI** của các module trong hệ thống Testify Core.

---

## 🌐 Swagger UI (Giao diện API tương tác)

Dự án sử dụng SpringDoc OpenAPI và `therapi-runtime-javadoc` để tự động chuyển comment Javadoc thành tài liệu API đẹp mắt.

> **Lưu ý:** Bạn phải khởi chạy (Run) các service thì mới có thể truy cập được các link này. Nếu bạn vừa sửa comment Javadoc, bạn cần Restart lại service (hoặc Rebuild project) để Swagger cập nhật.

- **Auth Service:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **Testify Service:** [http://localhost:9002/swagger-ui/index.html](http://localhost:9002/swagger-ui/index.html)

---

## 📖 Javadoc (Tài liệu mã nguồn tĩnh)

Đây là tài liệu HTML tĩnh, giống hệt tài liệu chuẩn của JDK.

> **Lưu ý:** Bạn cần mở Terminal ở thư mục `testify_core` và chạy lệnh `mvn clean install javadoc:javadoc -DskipTests` trước. Sau khi báo BUILD SUCCESS, các link dưới đây mới hoạt động.

Bạn có thể click trực tiếp vào các đường dẫn bên dưới (giữ `Ctrl` + Click) để mở bằng trình duyệt web:

- **Shared Kernel:** [shared-kernel/target/reports/apidocs/index.html](file:///d:/dung/testify/testify_core/shared-kernel/target/reports/apidocs/index.html)
- **Auth Service:** [auth-service/target/reports/apidocs/index.html](file:///d:/dung/testify/testify_core/auth-service/target/reports/apidocs/index.html)
- **Testify Service:** [testify-service/target/reports/apidocs/index.html](file:///d:/dung/testify/testify_core/testify-service/target/reports/apidocs/index.html)

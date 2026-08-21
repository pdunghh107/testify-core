package com.zcomini.backend.testify.config;

import com.zcomini.backend.testify.dto.LogMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class SpeedTestConfig {

    /**
     * Cấu hình HttpClient siêu tốc hỗ trợ HTTP/2 và Virtual Threads.
     * Sử dụng Connection Pool và Multiplexing mặc định của Java 21 HttpClient.
     */
    @Bean
    public HttpClient optimizedHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(3)) // Timeout kết nối rất ngắn
                .executor(Executors.newVirtualThreadPerTaskExecutor()) // Dùng Virtual Thread cho các callback
                .build();
    }

    /**
     * Hàng đợi lưu trữ log để ghi file bất đồng bộ.
     * Sức chứa 10,000 phần tử để đệm kể cả khi API chạy quá nhanh.
     */
    @Bean
    public BlockingQueue<LogMessage> logQueue() {
        return new LinkedBlockingQueue<>(10000);
    }
}

package com.zcomini.backend.testify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.zcomini.backend.testify", "com.zcomini.backend.shared" })
public class TestifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestifyApplication.class, args);
    }
}

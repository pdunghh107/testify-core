package com.zcomini.backend.testify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "testify")
@Data
public class TestifyProperties {
    private String logDir;
    private int maxConcurrentRequests;
}

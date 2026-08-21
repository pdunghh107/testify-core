package com.zcomini.backend.auth.config.s3;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "s3", name = "enabled", havingValue = "true")
public class AWSConfig {
    @Value("${s3.service.access_key}")
    public String accessKey;
    @Value("${s3.service.secret_key}")
    public String secretKey;
    @Value("${s3.service.end_point}")
    public String endPoint;
    @Value("${s3.service.bucket}")
    public String bucket;

    @Bean
    public MinioClient minioS3() {
        return MinioClient.builder().endpoint(endPoint).credentials(accessKey, secretKey).build();
    }
}

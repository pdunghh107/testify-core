package com.zcomini.backend.testify.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Testify Service API", 
        version = "v1", 
        description = "Tài liệu API cho Testify Service thuộc dự án Testify. Hỗ trợ tự động đọc Javadoc để hiển thị Swagger."
    )
)
public class SwaggerConfig {
}

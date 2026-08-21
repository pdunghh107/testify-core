package com.zcomini.backend.auth.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình lõi (Core) cho RabbitMQ trong hệ thống Auth Service.
 * <p>
 * File này chứa các Bean mang tính chất Global, áp dụng chung cho mọi Domain
 * (ví dụ: Audit, Notification) thay vì phụ thuộc vào một tính năng cụ thể.
 */
@Configuration
public class RabbitMQCoreConfig {

    /**
     * Cấu hình MessageConverter dùng chung để serialize/deserialize Message sang định dạng JSON.
     * Cần thiết để các Object (ví dụ: Event DTO) có thể truyền qua RabbitMQ.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

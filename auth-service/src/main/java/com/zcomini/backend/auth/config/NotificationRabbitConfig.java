package com.zcomini.backend.auth.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitConfig {

    @Bean
    public TopicExchange notificationExchange(
            @Value("${app.notification.rabbit.exchange:notification.events}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

}

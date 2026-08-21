package com.zcomini.backend.auth.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditLogRabbitConfig {

    public static final String AUDIT_EXCHANGE = "testify.audit.exchange";
    public static final String AUDIT_QUEUE = "auth.audit.log.queue";
    public static final String AUDIT_ROUTING_KEY = "audit.log.#";

    // Cấu hình Dead Letter
    public static final String AUDIT_DLX_EXCHANGE = "testify.audit.dlx";
    public static final String AUDIT_DLQ_QUEUE = "auth.audit.log.dlq";

    @Bean
    TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }

    @Bean
    TopicExchange auditDlxExchange() {
        return new TopicExchange(AUDIT_DLX_EXCHANGE);
    }

    @Bean
    Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", AUDIT_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", AUDIT_ROUTING_KEY)
                .build();
    }

    @Bean
    Queue auditDlqQueue() {
        return QueueBuilder.durable(AUDIT_DLQ_QUEUE).build();
    }

    @Bean
    Binding auditBinding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue).to(auditExchange).with(AUDIT_ROUTING_KEY);
    }

    @Bean
    Binding auditDlqBinding(Queue auditDlqQueue, TopicExchange auditDlxExchange) {
        return BindingBuilder.bind(auditDlqQueue).to(auditDlxExchange).with(AUDIT_ROUTING_KEY);
    }
}

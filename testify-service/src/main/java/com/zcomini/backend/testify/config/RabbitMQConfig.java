package com.zcomini.backend.testify.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zcomini.backend.shared.rabbitmq.RabbitQueueBuilder;

@Configuration
public class RabbitMQConfig {

    @Value("${app.notification.rabbit.exchange:notification.events}")
    private String exchangeName;

    @Value("${app.testify.rabbit.queue.user.registered:testify.user.registered.queue}")
    private String userRegisteredQueue;

    // --- DLQ Properties ---
    @Value("${app.testify.rabbit.dlx:testify.dlx}")
    private String dlxName;

    @Value("${app.testify.rabbit.dlq.user.registered:testify.user.registered.dlq}")
    private String userRegisteredDlq;

    // --- Exchanges ---
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(dlxName, true, false);
    }

    // --- Queues ---
    @Bean
    public Queue userRegisteredQueue() {
        return RabbitQueueBuilder.buildQueueWithDlq(userRegisteredQueue, dlxName, "user.registered.dlq");
    }

    @Bean
    public Queue userRegisteredDlq() {
        return QueueBuilder.durable(userRegisteredDlq).build();
    }

    // --- Bindings ---
    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(notificationExchange).with("user.registered");
    }

    @Bean
    public Binding userRegisteredDlqBinding(Queue userRegisteredDlq, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(userRegisteredDlq).to(deadLetterExchange).with("user.registered.dlq");
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter jsonMessageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }
}

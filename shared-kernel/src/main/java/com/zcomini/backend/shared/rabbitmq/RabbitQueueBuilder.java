package com.zcomini.backend.shared.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

public class RabbitQueueBuilder {

    private RabbitQueueBuilder() {
    }

    public static Queue buildQueueWithDlq(String queueName, String dlxExchange, String dlqRoutingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }
}

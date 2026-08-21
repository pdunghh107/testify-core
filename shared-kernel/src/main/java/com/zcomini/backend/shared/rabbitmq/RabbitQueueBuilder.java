package com.zcomini.backend.shared.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

/**
 * Tiện ích hỗ trợ tạo Queue cho RabbitMQ với các cấu hình chuẩn mực (DLQ,
 * Retry).
 */
public class RabbitQueueBuilder {

    private RabbitQueueBuilder() {
        // Utility class
    }

    /**
     * Khởi tạo một Queue chính (Main Queue) có trỏ đến Dead Letter Exchange (DLX).
     * Khi message trong Queue này bị lỗi (hết số lần retry), nó sẽ tự động bị đẩy
     * sang DLX.
     *
     * @param queueName     Tên của Main Queue
     * @param dlxExchange   Tên của Dead Letter Exchange
     * @param dlqRoutingKey Routing key để điều hướng từ DLX vào Dead Letter Queue
     *                      (DLQ)
     * @return Queue đã được cấu hình DLX
     */
    public static Queue buildQueueWithDlq(String queueName, String dlxExchange, String dlqRoutingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }
}

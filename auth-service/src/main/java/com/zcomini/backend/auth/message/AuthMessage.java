package com.zcomini.backend.auth.message;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zcomini.backend.auth.entity.UserEntity;
import com.zcomini.backend.shared.event.UserRegisteredEvent;
import com.zcomini.backend.shared.util.StringCustom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j

public final class AuthMessage {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.notification.rabbit.exchange:notification.events}")
    private String exchangeName;

    public void sendUserCreatedMessage(UserEntity user, String email) {
        try {
            String workspaceName = StringCustom.extractUsernameFromEmail(email) + "'s Workspace";
            UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), user.getEmail(), workspaceName);
            rabbitTemplate.convertAndSend(exchangeName, "user.registered", event);
            log.info("[EVENT SEND]: Gửi sự kiện người dùng {} đăng ký thành công", user.getId());
        } catch (Exception e) {
            log.error("[EVENT SEND]: Gửi sự kiện người dùng đăng ký thất bại", e);
        }
    }

}

package com.zcomini.backend.testify.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.zcomini.backend.shared.event.UserRegisteredEvent;
import com.zcomini.backend.shared.tenant.RequestContext;
import com.zcomini.backend.testify.dto.request.CreateWorkspaceRequest;
import com.zcomini.backend.testify.service.WorkspaceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final WorkspaceService workspaceService;

    @RabbitListener(queues = "${app.testify.rabbit.queue.user.registered:testify.user.registered.queue}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Nhận được sự kiện UserRegisteredEvent cho user: {}", event.email());
        try {
            // Check for idempotency: if user already has a workspace, ignore event
            if (workspaceService.hasWorkspace(event.userId())) {
                log.info("User {} đã có workspace. Bỏ qua sự kiện (Idempotency).", event.userId());
                return;
            }

            // Set context for background thread
            RequestContext.setUserId(event.userId());

            CreateWorkspaceRequest request = new CreateWorkspaceRequest(
                    event.defaultWorkspaceName(),
                    "Môi trường làm việc mặc định của bạn"
            );

            workspaceService.createWorkspace(request);
            log.info("Đã tạo workspace mặc định thành công cho user: {}", event.userId());
        } catch (Exception e) {
            log.error("Tạo workspace mặc định thất bại cho user: {}", event.userId(), e);
            throw e; 
        } finally {
            // Clean up ThreadLocal
            RequestContext.clear();
        }
    }
}

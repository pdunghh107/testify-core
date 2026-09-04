package com.zcomini.backend.shared.event;

import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String email,
        String defaultWorkspaceName) {
}

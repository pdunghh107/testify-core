package com.zcomini.backend.auth.client.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record HostAppIdentity(
                String subject,
                String fullName,
                String citizenIdNumber,
                String phone,
                String dateOfBirth,
                JsonNode rawPayload) {
}

package com.zcomini.backend.testify.dto;

public record TestLogDTO(
        int caseId,
        String targetUrl,
        int expectedStatus,
        int actualStatus,
        String fileWriteStatus,
        String message,
        String payload,
        String responseBody) {
}

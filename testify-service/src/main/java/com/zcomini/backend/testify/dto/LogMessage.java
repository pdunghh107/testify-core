package com.zcomini.backend.testify.dto;

public record LogMessage(
        int caseId,
        String description,
        String payload,
        String testResult,
        String actualStatus,
        String responseBody) {
}

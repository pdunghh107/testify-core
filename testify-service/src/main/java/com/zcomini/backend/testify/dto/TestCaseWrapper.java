package com.zcomini.backend.testify.dto;

import java.util.Map;

public record TestCaseWrapper(
        String description,
        Map<String, Object> payload,
        int expectedStatusCode) {
}

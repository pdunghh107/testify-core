package com.zcomini.backend.testify.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.zcomini.backend.testify.dto.LogMessage;
import com.zcomini.backend.testify.dto.TestLogDTO;
import com.zcomini.backend.testify.engine.FieldRule;
import com.zcomini.backend.testify.engine.RuleBasedDataGeneratorEngine;
import com.zcomini.backend.testify.entity.RuleConfig;
import com.zcomini.backend.testify.repository.RuleConfigRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleRunnerService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final BlockingQueue<LogMessage> logQueue;
    private final RuleConfigRepository ruleConfigRepository;
    private final RuleBasedDataGeneratorEngine dataGeneratorEngine;

    @SuppressWarnings("unchecked")
    public void executeTestMatrix(Map<String, Object> requestInput, SseEmitter emitter) {
        String targetUrl = (String) requestInput.get("url");
        String method = (String) requestInput.getOrDefault("method", "POST");
        Map<String, String> headers = (Map<String, String>) requestInput.get("headers");
        String ruleConfigCode = (String) requestInput.get("ruleConfigCode");

        // 1. Fetch Rules from DB
        RuleConfig ruleConfig = ruleConfigRepository.findByConfigCode(ruleConfigCode)
                .orElseThrow(() -> new IllegalArgumentException("RuleConfig not found with code: " + ruleConfigCode));

        Map<String, FieldRule> rules = dataGeneratorEngine.parseRules(ruleConfig.getRules());

        // 2. Generate payloads
        List<Map<String, Object>> testSuite = new ArrayList<>();

        // Happy case
        testSuite.add(dataGeneratorEngine.generateHappyCase(rules));

        // Negative cases
        testSuite.addAll(dataGeneratorEngine.generateNegativeCases(rules));

        // Send START event
        try {
            emitter.send(SseEmitter.event().name("START").data("{\"totalCases\": " + testSuite.size() + "}"));
        } catch (Exception e) {
            log.error("Lỗi khi gửi sự kiện START", e);
        }

        // 3. Execute via Virtual Threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < testSuite.size(); i++) {
                final int currentCaseId = i + 1; // 1-indexed for logs
                final Map<String, Object> payload = testSuite.get(i);
                final boolean isHappyCase = (i == 0); // First one is Happy Case
                final String description = isHappyCase ? "Happy Case" : "Negative Case #" + i;
                final int expectedStatus = isHappyCase ? 200 : 400; // Simplified assumption

                executor.submit(() -> {
                    runAndAssertTestCase(targetUrl, method, headers, payload, expectedStatus, description,
                            currentCaseId, emitter);
                });
            }
        }

        // Hoàn tất luồng
        try {
            emitter.send(SseEmitter.event().name("COMPLETED").data("All tests submitted"));
            emitter.complete();
        } catch (Exception e) {
            log.error("Lỗi khi gửi sự kiện COMPLETED", e);
        }
    }

    private void runAndAssertTestCase(String url, String method, Map<String, String> headers,
            Map<String, Object> payload,
            int expectedStatus, String description, int caseId, SseEmitter emitter) {
        String jsonPayload = "";
        int actualStatus = 500;
        String testResult = "FAILED ❌";
        String responseBodyStr = "";

        try {
            jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method(method, HttpRequest.BodyPublishers.ofString(jsonPayload));

            if (headers != null) {
                headers.forEach(requestBuilder::header);
            }
            if (headers == null || !headers.containsKey("Content-Type")) {
                requestBuilder.header("Content-Type", "application/json");
            }

            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            actualStatus = response.statusCode();
            responseBodyStr = response.body();

            // Lỏng lẻo hơn cho Happy Case: 200, 201, 2xx là được
            boolean isPassed = false;
            if (expectedStatus == 200) {
                isPassed = (actualStatus >= 200 && actualStatus < 300);
            } else {
                isPassed = (actualStatus >= 400); // Các lỗi 4xx
            }

            testResult = isPassed ? "PASSED ✅" : "FAILED ❌";

        } catch (Exception e) {
            log.error("Network / Parse Error for Case {}", caseId, e);
            testResult = "SYSTEM_ERROR 🚨";
            responseBodyStr = e.getMessage();
        } finally {
            LogMessage logMessage = new LogMessage(
                    caseId, description, jsonPayload, testResult,
                    "HTTP " + actualStatus + " (Kỳ vọng: " + expectedStatus + ")",
                    responseBodyStr);
            logQueue.offer(logMessage);

            try {
                TestLogDTO dto = new TestLogDTO(
                        caseId, url, expectedStatus, actualStatus,
                        "QUEUED", testResult + " - " + description,
                        jsonPayload, responseBodyStr);
                emitter.send(SseEmitter.event().name("LOG_EVENT").data(dto));
            } catch (Exception ex) {
                // Ignore SSE errors if client disconnected
            }
        }
    }
}

package com.zcomini.backend.testify.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import com.zcomini.backend.testify.dto.request.ValidateRuleRequest;
import com.zcomini.backend.testify.engine.RequestMappingEngine;
import com.zcomini.backend.testify.entity.FieldConfig;
import com.zcomini.backend.testify.service.RuleRunnerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rules-runner")
@RequiredArgsConstructor
public class RuleRunnerController {

    private final RuleRunnerService ruleRunnerService;
    private final RequestMappingEngine requestMappingEngine;
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    @PostMapping("/run-stream")
    public SseEmitter runTestsStream(@RequestBody Map<String, Object> requestInput) {
        // SSE timeout 5 phút (300_000ms)
        SseEmitter emitter = new SseEmitter(300_000L);

        // Chạy ngầm tránh block HTTP thread chính
        sseExecutor.execute(() -> {
            try {
                ruleRunnerService.executeTestMatrix(requestInput, emitter);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/validate")
    public Map<String, String> validateRule(@RequestBody ValidateRuleRequest request) {
        if (request.bodyTemplate() == null || request.ruleConfigCode() == null || request.workspaceId() == null) {
            throw new IllegalArgumentException("Thiếu thông tin bắt buộc (bodyTemplate, ruleConfigCode, workspaceId)");
        }

        List<String> extractedKeys = requestMappingEngine.extractKeysFromBody(request.bodyTemplate());
        List<FieldConfig> matchedFields = requestMappingEngine.matchFieldConfigs(extractedKeys, request.workspaceId());
        String warning = requestMappingEngine.validateRuleCompatibility(request.ruleConfigCode(), matchedFields);

        if (warning != null) {
            return Map.of("status", "warning", "message", warning);
        }

        return Map.of("status", "ok");
    }
}

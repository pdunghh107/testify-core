package com.zcomini.backend.testify.controller;

import com.zcomini.backend.testify.service.PermutationTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/permutations")
@RequiredArgsConstructor
public class PermutationTestController {

    private final PermutationTestService testService;

    @PostMapping(value = "/run-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter runTestStream(@RequestBody Map<String, Object> requestInput) {
        // Timeout 10 phút (600,000 ms)
        SseEmitter emitter = new SseEmitter(600000L);

        emitter.onCompletion(() -> log.info("SseEmitter is completed"));
        emitter.onTimeout(() -> {
            log.warn("SseEmitter timeout");
            emitter.complete();
        });
        emitter.onError(e -> log.error("SseEmitter error", e));

        // Khởi tạo luồng ảo chạy ngầm test case
        Thread.ofVirtual().start(() -> {
            try {
                testService.executeTestMatrix(requestInput, emitter);
            } catch (Exception e) {
                log.error("Lỗi khởi chạy test stream", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}

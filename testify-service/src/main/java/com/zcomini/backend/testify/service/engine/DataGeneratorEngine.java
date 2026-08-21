package com.zcomini.backend.testify.service.engine;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class DataGeneratorEngine {

    // Thread-safe counter cho đa luồng
    private final AtomicInteger counter = new AtomicInteger(1);

    @SuppressWarnings("unchecked")
    public Map<String, Object> applyGenerators(Map<String, Object> basePayload, Map<String, Object> generatorsConfig) {
        Map<String, Object> dynamicPayload = new HashMap<>();
        if (basePayload != null) {
            dynamicPayload.putAll(basePayload);
        }
        if (generatorsConfig == null)
            return dynamicPayload;

        generatorsConfig.forEach((fieldKey, configObj) -> {
            Map<String, Object> config = (Map<String, Object>) configObj;
            String type = (String) config.get("type");
            String prefix = (String) config.get("prefix");
            if (prefix == null)
                prefix = "";

            if ("prefix_counter".equals(type)) {
                int nextId = counter.getAndIncrement();
                dynamicPayload.put(fieldKey, prefix + nextId);
            } else if ("timestamp_email".equals(type)) {
                long timestamp = Instant.now().toEpochMilli();
                dynamicPayload.put(fieldKey, prefix + "_" + timestamp + "@example.com");
            } else if ("timestamp_numeric".equals(type)) {
                long timestamp = Instant.now().toEpochMilli();
                dynamicPayload.put(fieldKey, prefix + timestamp);
            }
        });

        return dynamicPayload;
    }
}

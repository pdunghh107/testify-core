package com.zcomini.backend.testify.engine.strategy;

import com.zcomini.backend.testify.engine.FieldRule;
import net.datafaker.Faker;

import java.util.List;
import java.util.Map;

public interface DataGeneratorStrategy {
    boolean supports(FieldRule rule);
    Object generateHappyValue(FieldRule rule, Faker faker);
    List<Map<String, Object>> generateNegativeCases(String fieldName, FieldRule rule, Map<String, Object> baseHappyPayload, Faker faker);
}

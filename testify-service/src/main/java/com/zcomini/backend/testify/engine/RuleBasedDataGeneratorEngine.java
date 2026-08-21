package com.zcomini.backend.testify.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcomini.backend.testify.engine.strategy.DataGeneratorStrategy;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("ruleBasedDataGeneratorEngine")
@RequiredArgsConstructor
public class RuleBasedDataGeneratorEngine {

    private final Faker faker = new Faker();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<DataGeneratorStrategy> strategies;

    public Map<String, FieldRule> parseRules(Map<String, Object> rawRules) {
        return objectMapper.convertValue(rawRules, new TypeReference<Map<String, FieldRule>>() {});
    }

    public Map<String, Object> generateHappyCase(Map<String, FieldRule> rules) {
        Map<String, Object> payload = new HashMap<>();
        
        for (Map.Entry<String, FieldRule> entry : rules.entrySet()) {
            String fieldName = entry.getKey();
            FieldRule rule = entry.getValue();
            
            Object validValue = generateValidValue(rule);
            payload.put(fieldName, validValue);
        }
        
        return payload;
    }

    public List<Map<String, Object>> generateNegativeCases(Map<String, FieldRule> rules) {
        List<Map<String, Object>> negativeCases = new ArrayList<>();
        Map<String, Object> baseHappyPayload = generateHappyCase(rules);

        for (Map.Entry<String, FieldRule> entry : rules.entrySet()) {
            String fieldName = entry.getKey();
            FieldRule rule = entry.getValue();

            // 1. Missing if required
            if (rule.isRequired()) {
                Map<String, Object> missingPayload = new HashMap<>(baseHappyPayload);
                missingPayload.remove(fieldName);
                negativeCases.add(missingPayload);

                Map<String, Object> nullPayload = new HashMap<>(baseHappyPayload);
                nullPayload.put(fieldName, null);
                negativeCases.add(nullPayload);
            }

            // 2. Format specific negative cases using strategies
            DataGeneratorStrategy strategy = getStrategy(rule);
            if (strategy != null) {
                negativeCases.addAll(strategy.generateNegativeCases(fieldName, rule, baseHappyPayload, faker));
            }
        }

        return negativeCases;
    }

    private Object generateValidValue(FieldRule rule) {
        DataGeneratorStrategy strategy = getStrategy(rule);
        if (strategy != null) {
            return strategy.generateHappyValue(rule, faker);
        }
        return faker.lorem().word();
    }

    private DataGeneratorStrategy getStrategy(FieldRule rule) {
        for (DataGeneratorStrategy strategy : strategies) {
            if (strategy.supports(rule)) {
                return strategy;
            }
        }
        return null;
    }
}

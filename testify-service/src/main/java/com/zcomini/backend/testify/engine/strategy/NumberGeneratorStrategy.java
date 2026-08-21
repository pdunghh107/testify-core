package com.zcomini.backend.testify.engine.strategy;

import com.zcomini.backend.testify.engine.FieldRule;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NumberGeneratorStrategy implements DataGeneratorStrategy {

    private static final List<String> SUPPORTED_TYPES = Arrays.asList("integer", "number");

    @Override
    public boolean supports(FieldRule rule) {
        String type = rule.getType() != null ? rule.getType().toLowerCase() : "string";
        return SUPPORTED_TYPES.contains(type) && (rule.getEnumValues() == null || rule.getEnumValues().isEmpty());
    }

    @Override
    public Object generateHappyValue(FieldRule rule, Faker faker) {
        int min = rule.getMin() != null ? rule.getMin() : 1;
        int max = rule.getMax() != null ? rule.getMax() : 1000;
        if (min > max) max = min + 100;
        return faker.number().numberBetween(min, max);
    }

    @Override
    public List<Map<String, Object>> generateNegativeCases(String fieldName, FieldRule rule, Map<String, Object> baseHappyPayload, Faker faker) {
        List<Map<String, Object>> cases = new ArrayList<>();

        // Wrong type
        Map<String, Object> wrongType = new HashMap<>(baseHappyPayload);
        wrongType.put(fieldName, faker.lorem().word()); // Use string for number
        cases.add(wrongType);

        if (rule.getMin() != null) {
            Map<String, Object> tooSmall = new HashMap<>(baseHappyPayload);
            tooSmall.put(fieldName, rule.getMin() - 1);
            cases.add(tooSmall);
        }
        if (rule.getMax() != null) {
            Map<String, Object> tooLarge = new HashMap<>(baseHappyPayload);
            tooLarge.put(fieldName, rule.getMax() + 1);
            cases.add(tooLarge);
        }

        return cases;
    }
}

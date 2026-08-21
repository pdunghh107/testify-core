package com.zcomini.backend.testify.engine.strategy;

import com.zcomini.backend.testify.engine.FieldRule;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BooleanGeneratorStrategy implements DataGeneratorStrategy {

    @Override
    public boolean supports(FieldRule rule) {
        String type = rule.getType() != null ? rule.getType().toLowerCase() : "string";
        return "boolean".equals(type) && (rule.getEnumValues() == null || rule.getEnumValues().isEmpty());
    }

    @Override
    public Object generateHappyValue(FieldRule rule, Faker faker) {
        return faker.bool().bool();
    }

    @Override
    public List<Map<String, Object>> generateNegativeCases(String fieldName, FieldRule rule, Map<String, Object> baseHappyPayload, Faker faker) {
        List<Map<String, Object>> cases = new ArrayList<>();
        Map<String, Object> wrongType = new HashMap<>(baseHappyPayload);
        wrongType.put(fieldName, "not-a-boolean");
        cases.add(wrongType);
        return cases;
    }
}

package com.zcomini.backend.testify.engine.strategy;

import com.zcomini.backend.testify.engine.FieldRule;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EnumGeneratorStrategy implements DataGeneratorStrategy {

    @Override
    public boolean supports(FieldRule rule) {
        return rule.getEnumValues() != null && !rule.getEnumValues().isEmpty();
    }

    @Override
    public Object generateHappyValue(FieldRule rule, Faker faker) {
        int index = faker.random().nextInt(rule.getEnumValues().size());
        return rule.getEnumValues().get(index);
    }

    @Override
    public List<Map<String, Object>> generateNegativeCases(String fieldName, FieldRule rule, Map<String, Object> baseHappyPayload, Faker faker) {
        List<Map<String, Object>> cases = new ArrayList<>();
        
        // Sinh chuỗi ngẫu nhiên
        String badValue;
        int attempts = 0;
        do {
            badValue = faker.lorem().word() + faker.number().digits(3);
            attempts++;
        } while (rule.getEnumValues().contains(badValue) && attempts < 10);
        
        Map<String, Object> badEnumPayload = new HashMap<>(baseHappyPayload);
        badEnumPayload.put(fieldName, badValue);
        cases.add(badEnumPayload);

        return cases;
    }
}

package com.zcomini.backend.testify.engine.strategy;

import com.zcomini.backend.testify.engine.FieldRule;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StringGeneratorStrategy implements DataGeneratorStrategy {

    private static final List<String> SUPPORTED_TYPES = Arrays.asList("string", "email", "uuid", "regex");

    @Override
    public boolean supports(FieldRule rule) {
        String type = rule.getType() != null ? rule.getType().toLowerCase() : "string";
        return SUPPORTED_TYPES.contains(type) && (rule.getEnumValues() == null || rule.getEnumValues().isEmpty());
    }

    @Override
    public Object generateHappyValue(FieldRule rule, Faker faker) {
        String type = rule.getType() != null ? rule.getType().toLowerCase() : "string";
        if ("email".equals(type) || "email".equalsIgnoreCase(rule.getFormat())) {
            return faker.internet().emailAddress();
        }
        if ("uuid".equals(type) || "uuid".equalsIgnoreCase(rule.getFormat())) {
            return UUID.randomUUID().toString();
        }
        if ("phone".equalsIgnoreCase(rule.getFormat())) {
            return faker.phoneNumber().cellPhone();
        }
        
        if (rule.getPattern() != null && !rule.getPattern().isEmpty()) {
            try {
                return faker.regexify(rule.getPattern());
            } catch (Exception e) {
                // Ignore
            }
        }

        int min = rule.getMinLength() != null ? rule.getMinLength() : 5;
        int max = rule.getMaxLength() != null ? rule.getMaxLength() : 20;
        if (min > max) max = min + 10;
        
        return faker.lorem().characters(min, max, true, true);
    }

    @Override
    public List<Map<String, Object>> generateNegativeCases(String fieldName, FieldRule rule, Map<String, Object> baseHappyPayload, Faker faker) {
        List<Map<String, Object>> cases = new ArrayList<>();
        String type = rule.getType() != null ? rule.getType().toLowerCase() : "string";

        // Wrong type
        Map<String, Object> wrongType = new HashMap<>(baseHappyPayload);
        wrongType.put(fieldName, faker.number().numberBetween(1, 100)); // Use number for string
        cases.add(wrongType);

        if ("string".equals(type) || "regex".equals(type)) {
            if (rule.getMinLength() != null && rule.getMinLength() > 0) {
                Map<String, Object> tooShort = new HashMap<>(baseHappyPayload);
                tooShort.put(fieldName, faker.lorem().characters(0, rule.getMinLength() - 1, true, true));
                cases.add(tooShort);
            }
            if (rule.getMaxLength() != null) {
                Map<String, Object> tooLong = new HashMap<>(baseHappyPayload);
                tooLong.put(fieldName, faker.lorem().characters(rule.getMaxLength() + 1, rule.getMaxLength() + 10, true, true));
                cases.add(tooLong);
            }
        }

        if ("email".equals(type) || "email".equalsIgnoreCase(rule.getFormat())) {
            Map<String, Object> badEmail = new HashMap<>(baseHappyPayload);
            badEmail.put(fieldName, "invalid-email-format");
            cases.add(badEmail);
        }
        if ("uuid".equals(type) || "uuid".equalsIgnoreCase(rule.getFormat())) {
            Map<String, Object> badUuid = new HashMap<>(baseHappyPayload);
            badUuid.put(fieldName, "not-a-valid-uuid");
            cases.add(badUuid);
        }

        return cases;
    }
}

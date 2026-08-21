package com.zcomini.backend.testify.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcomini.backend.testify.entity.FieldConfig;
import com.zcomini.backend.testify.entity.RuleConfig;
import com.zcomini.backend.testify.exception.RuleConfigException;
import com.zcomini.backend.testify.repository.FieldConfigRepository;
import com.zcomini.backend.testify.repository.RuleConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestMappingEngine {

    private final ObjectMapper objectMapper;
    private final FieldConfigRepository fieldConfigRepository;
    private final RuleConfigRepository ruleConfigRepository;

    /**
     * Parse JSON body and extract all keys (including nested keys).
     */
    public List<String> extractKeysFromBody(String jsonBody) {
        Set<String> keys = new HashSet<>();
        if (jsonBody == null || jsonBody.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            JsonNode rootNode = objectMapper.readTree(jsonBody);
            extractKeysRecursive(rootNode, keys);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON body for key extraction", e);
        }
        return new ArrayList<>(keys);
    }

    private void extractKeysRecursive(JsonNode node, Set<String> keys) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                keys.add(field.getKey());
                extractKeysRecursive(field.getValue(), keys);
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                extractKeysRecursive(arrayItem, keys);
            }
        }
    }

    /**
     * Match extracted keys against the workspace's FieldConfigs.
     * Returns a list of FieldConfigs that match the keys in the body.
     */
    public List<FieldConfig> matchFieldConfigs(List<String> keys, UUID workspaceId) {
        List<FieldConfig> allConfigs = fieldConfigRepository.findByWorkspaceId(workspaceId);
        List<FieldConfig> matchedConfigs = new ArrayList<>();

        for (FieldConfig config : allConfigs) {
            List<String> containsKeywords = config.getContainsKeywords();
            if (containsKeywords == null || containsKeywords.isEmpty()) {
                continue;
            }

            boolean isMatched = keys.stream().anyMatch(key -> containsKeywords.stream()
                    .anyMatch(keyword -> key.toLowerCase().contains(keyword.toLowerCase())));

            if (isMatched) {
                matchedConfigs.add(config);
            }
        }
        return matchedConfigs;
    }

    /**
     * Validate if the selected Rule is compatible with the Request's matched
     * FieldConfigs.
     * Returns a Warning message if incompatible, or null if perfectly compatible.
     */
    public String validateRuleCompatibility(String ruleConfigCode, List<FieldConfig> matchedFields) {
        RuleConfig rule = ruleConfigRepository.findByConfigCode(ruleConfigCode)
                .orElseThrow(RuleConfigException::notFound);

        Map<String, Object> ruleConfigData = rule.getRules();

        // Assume ruleConfigData contains "target_field_names" array (e.g. ["phone",
        // "email"])
        // If not present, we assume it's a generic rule and skip validation.
        if (!ruleConfigData.containsKey("target_field_names")) {
            return null; // No specific target, assume compatible
        }

        @SuppressWarnings("unchecked")
        List<String> targetFieldNames = (List<String>) ruleConfigData.get("target_field_names");

        boolean hasOverlap = matchedFields.stream()
                .anyMatch(field -> targetFieldNames.contains(field.getName()));

        if (!hasOverlap) {
            return String.format(
                    "Cảnh báo: Rule '%s' yêu cầu các trường dữ liệu thuộc loại %s, nhưng Request Body của bạn không chứa trường nào phù hợp. Việc sinh hoán vị có thể không hiệu quả.",
                    rule.getName(), targetFieldNames);
        }

        return null; // Compatible
    }
}

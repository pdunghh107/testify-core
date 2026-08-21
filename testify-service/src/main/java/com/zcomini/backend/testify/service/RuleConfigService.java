package com.zcomini.backend.testify.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zcomini.backend.testify.entity.RuleConfig;
import com.zcomini.backend.testify.repository.RuleConfigRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RuleConfigService {

    private final RuleConfigRepository ruleConfigRepository;

    @Transactional(readOnly = true)
    public List<RuleConfig> getAllRuleConfigs() {
        return ruleConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RuleConfig getRuleConfigById(UUID id) {
        return ruleConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RuleConfig not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public RuleConfig getRuleConfigByCode(String code) {
        return ruleConfigRepository.findByConfigCode(code)
                .orElseThrow(() -> new IllegalArgumentException("RuleConfig not found with code: " + code));
    }

    @Transactional
    public RuleConfig createRuleConfig(RuleConfig ruleConfig) {
        if (ruleConfigRepository.existsByConfigCode(ruleConfig.getConfigCode())) {
            throw new IllegalArgumentException(
                    "RuleConfig with code '" + ruleConfig.getConfigCode() + "' already exists.");
        }
        return ruleConfigRepository.save(ruleConfig);
    }

    @Transactional
    public RuleConfig updateRuleConfig(UUID id, RuleConfig updatedConfig) {
        RuleConfig existing = getRuleConfigById(id);

        // Check code uniqueness if changed
        if (!existing.getConfigCode().equals(updatedConfig.getConfigCode()) &&
                ruleConfigRepository.existsByConfigCode(updatedConfig.getConfigCode())) {
            throw new IllegalArgumentException(
                    "RuleConfig with code '" + updatedConfig.getConfigCode() + "' already exists.");
        }

        existing.setConfigCode(updatedConfig.getConfigCode());
        existing.setName(updatedConfig.getName());
        existing.setRules(updatedConfig.getRules());

        return ruleConfigRepository.save(existing);
    }

    @Transactional
    public void deleteRuleConfig(UUID id) {
        if (!ruleConfigRepository.existsById(id)) {
            throw new IllegalArgumentException("RuleConfig not found with id: " + id);
        }
        ruleConfigRepository.deleteById(id);
    }
}

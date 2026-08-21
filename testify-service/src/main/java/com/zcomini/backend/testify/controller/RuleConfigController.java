package com.zcomini.backend.testify.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zcomini.backend.shared.api.dto.MessageResponse;
import com.zcomini.backend.testify.entity.RuleConfig;
import com.zcomini.backend.testify.service.RuleConfigService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rule-configs")
@RequiredArgsConstructor
public class RuleConfigController {

    private final RuleConfigService ruleConfigService;

    @GetMapping
    public List<RuleConfig> getAllRuleConfigs() {
        return ruleConfigService.getAllRuleConfigs();
    }

    @GetMapping("/{id}")
    public RuleConfig getRuleConfigById(@PathVariable UUID id) {
        return ruleConfigService.getRuleConfigById(id);
    }

    @GetMapping("/code/{code}")
    public RuleConfig getRuleConfigByCode(@PathVariable String code) {
        return ruleConfigService.getRuleConfigByCode(code);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleConfig createRuleConfig(@RequestBody RuleConfig ruleConfig) {
        return ruleConfigService.createRuleConfig(ruleConfig);
    }

    @PutMapping("/{id}")
    public RuleConfig updateRuleConfig(@PathVariable UUID id, @RequestBody RuleConfig ruleConfig) {
        return ruleConfigService.updateRuleConfig(id, ruleConfig);
    }

    @DeleteMapping("/{id}")
    public MessageResponse deleteRuleConfig(@PathVariable UUID id) {
        ruleConfigService.deleteRuleConfig(id);
        return new MessageResponse("Xóa RuleConfig thành công.");
    }
}

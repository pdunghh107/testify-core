package com.zcomini.backend.testify.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zcomini.backend.testify.entity.TestRule;
import com.zcomini.backend.testify.repository.TestRuleRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class TestRuleController {

    private final TestRuleRepository testRuleRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestRule createRule(@RequestBody TestRule rule) {
        return testRuleRepository.save(rule);
    }

    @GetMapping
    public List<TestRule> getAllRules() {
        return testRuleRepository.findAll();
    }

    // @GetMapping("/{ruleCode}")
    // public TestRule getRuleByCode(@PathVariable String ruleCode) {
    // TestRule rule = testRuleRepository.findByRuleCode(ruleCode)
    // .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy rule code: "
    // + ruleCode));
    // return rule;
    // }
}

package com.zcomini.backend.testify.repository;

import com.zcomini.backend.testify.entity.TestRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TestRuleRepository extends JpaRepository<TestRule, UUID> {
    Optional<TestRule> findByRuleCode(String ruleCode);
}

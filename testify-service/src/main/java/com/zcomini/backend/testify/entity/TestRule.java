package com.zcomini.backend.testify.entity;

import com.zcomini.backend.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "test_rules")
@Getter
@Setter
public class TestRule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_code", unique = true, nullable = false)
    private String ruleCode;

    @Column(name = "name")
    private String name;

    /**
     * Chứa chuỗi JSON Schema cấu hình bao gồm required_fields, generators, constraints.
     */
    @Column(name = "payload_schema", columnDefinition = "TEXT")
    private String payloadSchema;
}

package com.zcomini.backend.shared.api.enums;

public enum ApiSuccessCode {
    OK("OK"),
    CREATED("CREATED"),
    PAGED("PAGED"),
    MESSAGE("MESSAGE");

    private final String value;

    ApiSuccessCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

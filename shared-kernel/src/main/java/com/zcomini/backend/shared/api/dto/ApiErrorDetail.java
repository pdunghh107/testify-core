package com.zcomini.backend.shared.api.dto;

public record ApiErrorDetail(
        String code,
        String field,
        String message) {
}

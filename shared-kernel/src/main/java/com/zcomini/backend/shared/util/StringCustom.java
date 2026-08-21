package com.zcomini.backend.shared.util;

import java.util.Locale;

public class StringCustom {

    // ----------------------------------------------------------------
    // STRING HELPERS
    // ----------------------------------------------------------------
    public static String trimOrNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // 1. Chuẩn hóa email
    public static String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Trích xuất phần username từ địa chỉ email (phần trước ký tự @).
     * 
     * @param email Địa chỉ email
     * @return Chuỗi username, hoặc trả về nguyên gốc nếu không có ký tự @
     */
    public static String extractUsernameFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }
        return email;
    }
}

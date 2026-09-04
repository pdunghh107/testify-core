package com.zcomini.backend.shared.util;

public class StringCustom {

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

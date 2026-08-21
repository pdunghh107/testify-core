package com.zcomini.backend.shared.util;

import org.springframework.util.StringUtils;
import java.time.LocalDate;

public final class CommonUtils {

    private CommonUtils() {
    }

    public static LocalDate parseLocalDateSafe(String dateStr) {
        try {
            return StringUtils.hasText(dateStr) ? LocalDate.parse(dateStr) : null;
        } catch (Exception e) {
            return null;
        }
    }

}

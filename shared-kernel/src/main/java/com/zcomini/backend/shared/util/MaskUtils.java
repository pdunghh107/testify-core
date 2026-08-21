package com.zcomini.backend.shared.util;

public final class MaskUtils {
    private MaskUtils() {
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return email;
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2)
            return "***@" + parts[1];
        return name.substring(0, 2) + "***@" + parts[1];
    }

    public static String maskFullName(String name) {
        if (name == null || name.isEmpty())
            return name;
        String[] parts = name.split(" ");
        if (parts.length == 1)
            return parts[0].substring(0, 1) + "***";
        return parts[0] + " *** " + parts[parts.length - 1];
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4)
            return phone;
        return phone.substring(0, phone.length() - 4).replaceAll(".", "*") + phone.substring(phone.length() - 4);
    }

    public static String maskCitizenId(String id) {
        if (id == null || id.length() < 4)
            return id;
        return "***" + id.substring(id.length() - 4);
    }
}

package com.zcomini.backend.shared.tenant;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class LegacyRoleAccess {
    private LegacyRoleAccess() {
    }

    public static Set<String> modulesForRole(String role) {
        if (role == null || role.isBlank()) {
            return Set.of();
        }

        String normalized = role.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "super_admin" -> linked("dashboard", "catalog", "products", "suppliers", "warehouse", "orders",
                    "services", "bookings", "resources", "customers", "pricing", "loyalty", "affiliates",
                    "conversations", "invoices",
                    "content", "tickets", "users", "settings", "audit", "*");
            case "tenant_admin", "admin" -> linked("dashboard", "catalog", "products", "suppliers", "warehouse",
                    "orders", "services", "bookings", "resources", "customers", "pricing", "loyalty", "affiliates",
                    "conversations",
                    "invoices", "content", "tickets", "users", "settings");
            case "sales_staff" ->
                linked("dashboard", "catalog", "products", "orders", "customers", "pricing", "conversations");
            case "service_operator" ->
                linked("dashboard", "services", "bookings", "resources", "customers", "pricing", "loyalty");
            case "warehouse_staff", "warehouse_manager", "warehouse" ->
                linked("dashboard", "catalog", "products", "suppliers", "warehouse", "orders");
            case "accountant" -> linked("dashboard", "orders", "customers", "pricing", "invoices");
            case "customer_service", "cskh", "cs" ->
                linked("dashboard", "orders", "bookings", "customers", "conversations", "tickets");
            case "content_manager", "content", "content_marketing" -> linked("dashboard", "content", "conversations");
            default -> Set.of();
        };
    }

    public static Set<String> systemRolesForRole(String role) {
        if (role == null || role.isBlank()) {
            return Set.of();
        }
        if ("super_admin".equalsIgnoreCase(role.trim())) {
            return Set.of("super_admin");
        }
        return Set.of();
    }

    private static Set<String> linked(String... values) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }
}

package com.zcomini.backend.shared.tenant;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class RequestContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> MEMBERSHIP_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLE_IDS = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> MODULES = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> SYSTEM_ROLES = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> IP_ADDRESS = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ALLOW_NEGATIVE_STOCK = new ThreadLocal<>();
    private static final ThreadLocal<String> REFERRAL_CODE = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    public static UUID requireTenantId() {
        UUID tenantId = TENANT_ID.get();
        if (tenantId == null) {
            throw new IllegalStateException("Missing X-Tenant-Id header");
        }
        return tenantId;
    }

    public static void setUserId(UUID userId) {
        USER_ID.set(userId);
    }

    public static UUID getUserId() {
        return USER_ID.get();
    }

    public static void setMembershipId(UUID membershipId) {
        MEMBERSHIP_ID.set(membershipId);
    }

    public static UUID getMembershipId() {
        return MEMBERSHIP_ID.get();
    }

    public static void setUserRole(String role) {
        USER_ROLE.set(role);
    }

    public static String getUserRole() {
        return USER_ROLE.get();
    }

    public static void setRoleIds(List<String> roleIds) {
        ROLE_IDS.set(roleIds == null ? List.of() : List.copyOf(roleIds));
    }

    public static List<String> getRoleIds() {
        List<String> roleIds = ROLE_IDS.get();
        return roleIds == null ? List.of() : roleIds;
    }

    public static void setModules(Set<String> modules) {
        MODULES.set(modules == null ? Set.of() : Set.copyOf(modules));
    }

    public static Set<String> getModules() {
        Set<String> modules = MODULES.get();
        return modules == null ? Set.of() : modules;
    }

    public static boolean hasModule(String moduleCode) {
        Set<String> modules = getModules();
        return modules.contains("*") || modules.contains(moduleCode);
    }

    public static void setSystemRoles(Set<String> systemRoles) {
        SYSTEM_ROLES.set(systemRoles == null ? Set.of() : Set.copyOf(systemRoles));
    }

    public static Set<String> getSystemRoles() {
        Set<String> systemRoles = SYSTEM_ROLES.get();
        return systemRoles == null ? Set.of() : systemRoles;
    }

    public static boolean hasSystemRole(String roleCode) {
        return getSystemRoles().contains(roleCode);
    }

    public static boolean isSystemAdmin() {
        return hasSystemRole("super_admin") || "super_admin".equalsIgnoreCase(getUserRole());
    }

    public static boolean hasPermission(String permission) {
        return isSystemAdmin() || hasSystemRole(permission);
    }

    public static boolean isCrmStaff() {
        return isSystemAdmin() || getSystemRoles().stream().anyMatch(role -> role.startsWith("CRM_"));
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void setUserEmail(String email) {
        USER_EMAIL.set(email);
    }

    public static String getUserEmail() {
        return USER_EMAIL.get();
    }

    public static void setIpAddress(String ipAddress) {
        IP_ADDRESS.set(ipAddress);
    }

    public static String getIpAddress() {
        return IP_ADDRESS.get();
    }

    public static void setAllowNegativeStock(boolean allow) {
        ALLOW_NEGATIVE_STOCK.set(allow);
    }

    /** Returns true only if explicitly set to true (default false = safe). */
    public static boolean isAllowNegativeStock() {
        Boolean v = ALLOW_NEGATIVE_STOCK.get();
        return Boolean.TRUE.equals(v);
    }

    public static void setReferralCode(String code) {
        REFERRAL_CODE.set(code);
    }

    public static String getReferralCode() {
        return REFERRAL_CODE.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        MEMBERSHIP_ID.remove();
        USER_ROLE.remove();
        ROLE_IDS.remove();
        MODULES.remove();
        SYSTEM_ROLES.remove();
        REQUEST_ID.remove();
        USER_EMAIL.remove();
        IP_ADDRESS.remove();
        ALLOW_NEGATIVE_STOCK.remove();
        REFERRAL_CODE.remove();
    }
}

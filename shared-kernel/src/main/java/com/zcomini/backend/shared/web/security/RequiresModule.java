package com.zcomini.backend.shared.web.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method (or class) as requiring a specific module
 * in the active tenant's JWT claims.
 *
 * <p>
 * Apply at method or class level:
 * 
 * <pre>
 *   {@literal @}RequiresModule("products")
 *   {@literal @}GetMapping("/api/v1/products")
 *   public Page&lt;ProductResponse&gt; list() { ... }
 * </pre>
 *
 * <p>
 * The {@link ModuleGuardFilter} enforces this annotation before the
 * request reaches the controller. Tenants whose JWT does not contain the
 * required module code (or the wildcard {@code "*"}) receive HTTP 403.
 * Super-admins ({@code systemRoles} contains {@code "super_admin"})
 * are always allowed through.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresModule {
    /**
     * The module code that must be present in the active tenant's modules list.
     * Examples: {@code "products"}, {@code "bookings"}, {@code "warehouse"}.
     */
    String value();
}

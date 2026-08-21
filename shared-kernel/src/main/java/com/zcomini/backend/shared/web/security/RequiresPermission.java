package com.zcomini.backend.shared.web.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce granular permissions (actions) on Controller classes or
 * methods.
 * 
 * <p>
 * Example: {@code @RequiresPermission("CRM_TENANT_VIEW")}
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String value();
}

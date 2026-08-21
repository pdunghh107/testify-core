package com.zcomini.backend.shared.tenant;

import com.zcomini.backend.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class AuthorizationGuard {

    private AuthorizationGuard() {
    }

    public static void requireModule(String moduleCode) {
        if (RequestContext.isSystemAdmin()) {
            return;
        }
        if (!RequestContext.hasModule(moduleCode)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Missing module " + moduleCode);
        }
    }

    public static void requireSystemRole(String roleCode) {
        if (!RequestContext.hasSystemRole(roleCode)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Missing system role " + roleCode);
        }
    }

    public static void requirePermission(String permission) {
        if (!RequestContext.hasPermission(permission)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Missing permission " + permission);
        }
    }
}

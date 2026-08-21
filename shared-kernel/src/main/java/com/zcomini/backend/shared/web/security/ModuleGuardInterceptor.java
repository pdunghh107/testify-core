package com.zcomini.backend.shared.web.security;

// import com.zcomini.backend.shared.api.enums.ApiErrorCode;
// import com.zcomini.backend.shared.exception.BusinessException;
// import com.zcomini.backend.shared.tenant.RequestContext;
// import org.springframework.core.annotation.AnnotationUtils;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.zcomini.backend.shared.web.RequestContextFilter;

/**
 * Spring MVC interceptor (NOT a Servlet filter) that enforces
 * {@link RequiresModule}.
 *
 * <p>
 * Runs as a HandlerInterceptor — AFTER DispatcherServlet resolves the handler
 * and AFTER all Servlet filters (including {@link RequestContextFilter}).
 * This guarantees {@link RequestContext} is fully populated before the check.
 *
 * <p>
 * Must be registered in a {@code WebMvcConfigurer#addInterceptors} call.
 * {@link ModuleGuardAutoConfiguration} does this automatically.
 */
public class ModuleGuardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Object handler) throws Exception {

        // Tạm thời bypass phân quyền
        // if (true)
        // return true;

        /*
         * TODO: Bỏ comment toàn bộ đoạn code dưới đây khi muốn bật lại phân quyền
         * if (!(handler instanceof HandlerMethod method)) return true;
         * 
         * RequiresModule moduleAnnotation = resolveModuleAnnotation(method);
         * if (moduleAnnotation != null && !isModuleAllowed(moduleAnnotation.value())) {
         * throw new BusinessException(
         * HttpStatus.FORBIDDEN,
         * ApiErrorCode.FORBIDDEN.value(),
         * "Module '" + moduleAnnotation.value() +
         * "' is not enabled for the active tenant.");
         * }
         * 
         * RequiresPermission permissionAnnotation =
         * resolvePermissionAnnotation(method);
         * if (permissionAnnotation != null &&
         * !RequestContext.hasPermission(permissionAnnotation.value())) {
         * throw new BusinessException(
         * HttpStatus.FORBIDDEN,
         * ApiErrorCode.FORBIDDEN.value(),
         * "Permission '" + permissionAnnotation.value() +
         * "' is not granted for the active user.");
         * }
         */

        return true;
    }

    // private boolean isModuleAllowed(String moduleCode) {
    // if (RequestContext.isSystemAdmin())
    // return true;
    // return RequestContext.hasModule(moduleCode);
    // }

    // private RequiresModule resolveModuleAnnotation(HandlerMethod method) {
    // // Method-level first, then class-level
    // RequiresModule m = AnnotationUtils.findAnnotation(method.getMethod(),
    // RequiresModule.class);
    // return m != null ? m : AnnotationUtils.findAnnotation(method.getBeanType(),
    // RequiresModule.class);
    // }

    // private RequiresPermission resolvePermissionAnnotation(HandlerMethod method)
    // {
    // // Method-level first, then class-level
    // RequiresPermission p = AnnotationUtils.findAnnotation(method.getMethod(),
    // RequiresPermission.class);
    // return p != null ? p : AnnotationUtils.findAnnotation(method.getBeanType(),
    // RequiresPermission.class);
    // }
}

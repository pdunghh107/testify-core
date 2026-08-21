package com.zcomini.backend.shared.web.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto-configures {@link ModuleGuardInterceptor} for any Spring Boot web
 * application
 * that includes the shared-kernel dependency.
 *
 * <p>
 * Uses a Spring MVC {@link WebMvcConfigurer} interceptor (not a Servlet filter)
 * to guarantee execution order: RequestContextFilter runs first (populating
 * {@link com.zcomini.backend.shared.tenant.RequestContext}), then
 * DispatcherServlet
 * resolves the handler, then the interceptor's {@code preHandle} checks the
 * module.
 */
@Configuration
@ConditionalOnWebApplication
public class ModuleGuardAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ModuleGuardInterceptor())
                .addPathPatterns("/api/**");
    }
}

package com.zcomini.backend.shared.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.exception.BusinessException;
import com.zcomini.backend.shared.tenant.RequestContext;
import com.zcomini.backend.shared.web.security.ModuleGuardInterceptor;
import com.zcomini.backend.shared.web.security.RequiresModule;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ModuleGuardInterceptorTest {

    private final ModuleGuardInterceptor interceptor = new ModuleGuardInterceptor();

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    @Test
    void throwsBusinessExceptionWithStandardCodeWhenModuleIsDisabled() throws Exception {
        RequestContext.setModules(Set.of());

        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        HttpServletResponse response = new MockHttpServletResponse();
        Method method = GuardedController.class.getMethod("guarded");
        HandlerMethod handlerMethod = new HandlerMethod(new GuardedController(), method);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> interceptor.preHandle(request, response, handlerMethod));

        assertEquals(403, ex.getStatus().value());
        assertEquals(ApiErrorCode.FORBIDDEN.value(), ex.getCode());
    }

    static class GuardedController {
        @RequiresModule("catalog")
        public void guarded() {
        }
    }
}

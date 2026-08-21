package com.zcomini.backend.shared.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.zcomini.backend.shared.api.ApiResponse;
import com.zcomini.backend.shared.api.dto.PageResponse;

class ApiEnvelopeResponseBodyAdviceTest {

    private final ApiEnvelopeResponseBodyAdvice advice = new ApiEnvelopeResponseBodyAdvice();

    @Test
    void wrapsRegularJsonBodyInOkEnvelope() {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        Object wrapped = advice.beforeBodyWrite(
                new SampleBody("demo"),
                null,
                MediaType.APPLICATION_JSON,
                null,
                new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/api/test")),
                new ServletServerHttpResponse(servletResponse));

        ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);
        assertEquals("OK", response.code());
        assertInstanceOf(SampleBody.class, response.data());
        assertNull(response.message());
    }

    @Test
    void wrapsCreatedResponseInCreatedEnvelope() {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        servletResponse.setStatus(201);

        Object wrapped = advice.beforeBodyWrite(
                new SampleBody("created"),
                null,
                MediaType.APPLICATION_JSON,
                null,
                new ServletServerHttpRequest(new MockHttpServletRequest("POST", "/api/test")),
                new ServletServerHttpResponse(servletResponse));

        ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);
        assertEquals("CREATED", response.code());
        assertInstanceOf(SampleBody.class, response.data());
    }

    @Test
    void wrapsSpringPageInPagedEnvelope() {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        Object wrapped = advice.beforeBodyWrite(
                new PageImpl<>(List.of("a", "b")),
                null,
                MediaType.APPLICATION_JSON,
                null,
                new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/api/test")),
                new ServletServerHttpResponse(servletResponse));

        ApiResponse<?> response = assertInstanceOf(ApiResponse.class, wrapped);
        PageResponse<?> page = assertInstanceOf(PageResponse.class, response.data());
        assertEquals(List.of("a", "b"), page.items());
        assertEquals(2, page.totalElements());
    }

    private record SampleBody(String name) {
    }
}

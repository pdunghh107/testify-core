package com.zcomini.backend.shared.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;



import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestContextFilterTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();
    private final RequestContextFilter filter = new RequestContextFilter(
            "12345678901234567890123456789012",
            objectMapper,
            "test-service");

    @Test
    void returnsApiErrorForInvalidAccessToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertEquals("AUTH.TOKEN_INVALID", body.get("code").asText());
        assertEquals("test-service", body.get("service").asText());
    }

    @Test
    void returnsApiErrorForInvalidUuidHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Tenant-Id", "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(400, response.getStatus());
        assertEquals("COMMON.BAD_REQUEST", body.get("code").asText());
        assertEquals("test-service", body.get("service").asText());
    }
}

package com.zcomini.backend.shared.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.zcomini.backend.shared.api.enums.ApiErrorCode;
import com.zcomini.backend.shared.tenant.HeaderNames;
import com.zcomini.backend.shared.tenant.LegacyRoleAccess;
import com.zcomini.backend.shared.tenant.RequestContext;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("tenantRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;
    private final ObjectMapper objectMapper;
    private final String serviceName;

    public RequestContextFilter(
            @org.springframework.beans.factory.annotation.Value("${app.auth.jwt-secret:12345678901234567890123456789012}") String jwtSecret,
            ObjectMapper objectMapper,
            @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown-service}") String serviceName) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.objectMapper = objectMapper;
        this.serviceName = serviceName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
            if (!populateFromAuthorizationHeader(request, response)) {
                log.debug("Authorization header not found or invalid, trying other headers");
                populateFromHeaders(request);
            }
            log.debug("RequestContext populated: userId={}, tenantId={}, systemRoles={}",
                    RequestContext.getUserId(), RequestContext.getTenantId(), RequestContext.getSystemRoles());
            extractIpAddress(request);
            if (response.isCommitted()) {
                return;
            }
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException ex) {
            // UUID header (X-Tenant-Id / X-User-Id / X-Membership-Id) is not a valid UUID
            if (!response.isCommitted()) {
                ApiErrorResponseWriter.write(
                        objectMapper,
                        serviceName,
                        response,
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        ApiErrorCode.BAD_REQUEST.value(),
                        "Invalid UUID header value: " + ex.getMessage(),
                        request.getRequestURI(),
                        List.of(ex.getMessage()));
            }
        } finally {
            RequestContext.clear();
        }
    }

    private boolean populateFromAuthorizationHeader(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            return false;
        }
        try {
            String token = header.substring(7);
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            populateUuid(resolveTenantId(claims, request), RequestContext::setTenantId);
            populateUuid(resolveClaim(claims, "sub"), RequestContext::setUserId);
            populateUuid(resolveClaim(claims, "membershipId", request.getHeader(HeaderNames.MEMBERSHIP_ID)),
                    RequestContext::setMembershipId);

            String userRole = resolveClaim(claims, "role", request.getHeader(HeaderNames.USER_ROLE));
            RequestContext.setUserRole(userRole);

            List<String> roleIds = claimStrings(claims, "roleIds");
            if (roleIds.isEmpty()) {
                roleIds = splitHeaderValues(request.getHeader(HeaderNames.ROLE_IDS));
            }
            RequestContext.setRoleIds(roleIds);

            LinkedHashSet<String> modules = new LinkedHashSet<>(claimStrings(claims, "modules"));
            LinkedHashSet<String> systemRoles = new LinkedHashSet<>(claimStrings(claims, "systemRoles"));
            List<String> systemPermissions = claimStrings(claims, "systemPermissions");
            systemRoles.addAll(systemPermissions);

            if (modules.isEmpty()) {
                modules.addAll(splitHeaderValues(request.getHeader(HeaderNames.MODULES)));
            }
            if (systemRoles.isEmpty()) {
                systemRoles.addAll(splitHeaderValues(request.getHeader(HeaderNames.SYSTEM_ROLES)));
            }
            if (modules.isEmpty()) {
                modules.addAll(LegacyRoleAccess.modulesForRole(userRole));
            }
            if (systemRoles.isEmpty()) {
                systemRoles.addAll(LegacyRoleAccess.systemRolesForRole(userRole));
            }

            RequestContext.setModules(modules);
            RequestContext.setSystemRoles(systemRoles);
            RequestContext.setUserEmail(resolveClaim(claims, "email"));
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("Access token has expired");
            /*
             * TODO: Bỏ comment đoạn dưới đây khi muốn bật lại Auth
             * ApiErrorResponseWriter.write(
             * objectMapper,
             * serviceName,
             * response,
             * org.springframework.http.HttpStatus.UNAUTHORIZED,
             * ApiErrorCode.UNAUTHORIZED.value(),
             * "Access token has expired",
             * request.getRequestURI(),
             * List.of());
             * return true;
             */
            return false; // Tạm thời bypass auth
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid access token");
            /*
             * TODO: Bỏ comment đoạn dưới đây khi muốn bật lại Auth
             * ApiErrorResponseWriter.write(
             * objectMapper,
             * serviceName,
             * response,
             * org.springframework.http.HttpStatus.UNAUTHORIZED,
             * ApiErrorCode.UNAUTHORIZED.value(),
             * "Invalid access token",
             * request.getRequestURI(),
             * List.of());
             * return true;
             */
            return false; // Tạm thời bypass auth
        }
    }

    private void populateFromHeaders(HttpServletRequest request) {
        populateUuid(request.getHeader(HeaderNames.TENANT_ID), RequestContext::setTenantId);
        populateUuid(request.getHeader(HeaderNames.USER_ID), RequestContext::setUserId);
        populateUuid(request.getHeader(HeaderNames.MEMBERSHIP_ID), RequestContext::setMembershipId);

        String userRole = request.getHeader(HeaderNames.USER_ROLE);
        RequestContext.setUserRole(userRole);
        RequestContext.setRoleIds(splitHeaderValues(request.getHeader(HeaderNames.ROLE_IDS)));

        LinkedHashSet<String> modules = new LinkedHashSet<>(splitHeaderValues(request.getHeader(HeaderNames.MODULES)));
        LinkedHashSet<String> systemRoles = new LinkedHashSet<>(
                splitHeaderValues(request.getHeader(HeaderNames.SYSTEM_ROLES)));
        if (modules.isEmpty()) {
            modules.addAll(LegacyRoleAccess.modulesForRole(userRole));
        }
        if (systemRoles.isEmpty()) {
            systemRoles.addAll(LegacyRoleAccess.systemRolesForRole(userRole));
        }
        RequestContext.setModules(modules);
        RequestContext.setSystemRoles(systemRoles);
        RequestContext.setUserEmail(request.getHeader("X-User-Email"));
    }

    private String resolveTenantId(Claims claims, HttpServletRequest request) {
        return resolveClaim(
                claims,
                "activeTenantId",
                resolveClaim(claims, "tenantId", request.getHeader(HeaderNames.TENANT_ID)));
    }

    private String resolveClaim(Claims claims, String claimName) {
        Object value = "sub".equals(claimName) ? claims.getSubject() : claims.get(claimName);
        return value == null ? null : String.valueOf(value);
    }

    private String resolveClaim(Claims claims, String claimName, String fallback) {
        String value = resolveClaim(claims, claimName);
        return StringUtils.hasText(value) ? value : fallback;
    }

    private List<String> claimStrings(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof List<?> values) {
            return values.stream()
                    .map(String::valueOf)
                    .filter(StringUtils::hasText)
                    .toList();
        }
        return List.of();
    }

    private void populateUuid(String value, java.util.function.Consumer<UUID> consumer) {
        if (StringUtils.hasText(value)) {
            try {
                consumer.accept(UUID.fromString(value));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "'" + value + "' is not a valid UUID", ex);
            }
        }
    }

    private List<String> splitHeaderValues(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private void extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        RequestContext.setIpAddress(ip);
    }
}

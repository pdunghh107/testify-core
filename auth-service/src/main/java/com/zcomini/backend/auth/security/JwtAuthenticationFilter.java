package com.zcomini.backend.auth.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.zcomini.backend.auth.exception.TokenException;
import com.zcomini.backend.auth.service.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final StringRedisTemplate stringRedisTemplate;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthenticationFilter(JwtService jwtService,
            StringRedisTemplate stringRedisTemplate,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtService = jwtService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);

                if (isUserRevoked(claims)) {
                    logger.debug("JWT token is revoked because user was deactivated or logged out globally");
                    handlerExceptionResolver.resolveException(request, response, null,
                            TokenException.tokenRevoked());
                    return;
                }

                if (isTokenRevoked(claims)) {
                    logger.debug("JWT token is revoked individually (Logout current device)");
                    handlerExceptionResolver.resolveException(request, response, null,
                            TokenException.tokenRevoked());
                    return;
                }

                String role = claims.get("role", String.class);

                AuthenticatedUser principal = new AuthenticatedUser(
                        UUID.fromString(claims.getSubject()),
                        claims.get("email", String.class),
                        role);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        token,
                        authorities(role));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                // Ignore parsing errors (e.g. ExpiredJwtException, SignatureException)
                // The request will remain unauthenticated and Spring Security will handle it
                logger.error("Error authenticating JWT token", ex);
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> authorities(String role) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (StringUtils.hasText(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }

        return authorities;
    }

    private boolean isUserRevoked(Claims claims) {
        String subject = claims.getSubject();
        String blacklistKey = "user_revoked:" + subject;
        String revokedTimestampStr = stringRedisTemplate.opsForValue().get(blacklistKey);

        if (revokedTimestampStr != null && claims.getIssuedAt() != null) {
            long revokedTimestamp = Long.parseLong(revokedTimestampStr);
            long issuedAt = claims.getIssuedAt().getTime();
            return issuedAt < revokedTimestamp;
        }
        return false;
    }

    private boolean isTokenRevoked(Claims claims) {
        String jti = claims.getId();
        if (StringUtils.hasText(jti)) {
            String tokenBlacklistKey = "token_revoked:" + jti;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenBlacklistKey));
        }
        return false;
    }
}

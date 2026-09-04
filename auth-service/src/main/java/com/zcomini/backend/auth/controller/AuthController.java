package com.zcomini.backend.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zcomini.backend.auth.dto.request.ChangePasswordRequest;
import com.zcomini.backend.auth.dto.request.DeactivateRequest;
import com.zcomini.backend.auth.dto.request.LoginRequest;
import com.zcomini.backend.auth.dto.request.RegisterRequest;
import com.zcomini.backend.auth.dto.request.UserRequest;
import com.zcomini.backend.auth.dto.response.LoginResponse;
import com.zcomini.backend.auth.dto.response.RefreshResponse;
import com.zcomini.backend.auth.dto.response.RegisterResponse;
import com.zcomini.backend.auth.dto.response.UserResponse;
import com.zcomini.backend.auth.security.AuthenticatedUser;
import com.zcomini.backend.auth.service.AuthService;
import com.zcomini.backend.shared.api.dto.MessageResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        RegisterResponse response = authService.register(request, httpRequest.getRemoteAddr());
        setRefreshTokenCookie(httpResponse, response.refreshToken());
        return response;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request, httpRequest.getRemoteAddr());
        setRefreshTokenCookie(httpResponse, response.refreshToken());
        return response;
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @CookieValue String refreshToken, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        RefreshResponse response = authService.refresh(refreshToken, httpRequest.getRemoteAddr());
        setRefreshTokenCookie(httpResponse, response.refreshToken());
        return response;
    }

    @PostMapping("/logout")
    public MessageResponse logout(
            @CookieValue(required = false) String refreshToken,
            @org.springframework.web.bind.annotation.RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            HttpServletResponse httpResponse) {

        String accessToken = null;
        if (org.springframework.util.StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        MessageResponse msg = authService.logout(refreshToken, accessToken);
        clearRefreshTokenCookie(httpResponse);
        return msg;
    }

    @PostMapping("/logout-all")
    public MessageResponse logoutAll(
            Authentication authentication,
            HttpServletResponse httpResponse) {
        MessageResponse msg = authService.logoutAll((AuthenticatedUser) authentication.getPrincipal());
        clearRefreshTokenCookie(httpResponse);
        return msg;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getMe((AuthenticatedUser) authentication.getPrincipal());
    }

    @PutMapping("/me")
    public UserResponse updateMe(Authentication authentication,
            @Valid @RequestBody UserRequest request,
            HttpServletRequest httpRequest) {
        UserResponse response = authService.updateMe(
                (AuthenticatedUser) authentication.getPrincipal(),
                request,
                httpRequest.getRemoteAddr());
        return response;
    }

    @PostMapping("/me/password")
    public MessageResponse changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse msg = authService.changePassword((AuthenticatedUser) authentication.getPrincipal(), request);
        return msg;
    }

    @PostMapping("/me/deactivate")
    public MessageResponse deactivateAccount(Authentication authentication,
            @Valid @RequestBody DeactivateRequest request,
            @CookieValue(required = false) String refreshToken,
            HttpServletResponse httpResponse) {
        MessageResponse msg = authService.deactivateAccount(
                (AuthenticatedUser) authentication.getPrincipal(), request, refreshToken);
        clearRefreshTokenCookie(httpResponse);
        return msg;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

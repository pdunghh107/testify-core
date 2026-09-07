package com.zcomini.backend.auth.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zcomini.backend.auth.dto.request.ChangePasswordRequest;
import com.zcomini.backend.auth.dto.request.DeactivateRequest;
import com.zcomini.backend.auth.dto.request.LoginRequest;
import com.zcomini.backend.auth.dto.request.RegisterRequest;
import com.zcomini.backend.auth.dto.request.UserRequest;
import com.zcomini.backend.auth.dto.response.LoginResponse;
import com.zcomini.backend.auth.dto.response.RefreshResponse;
import com.zcomini.backend.auth.dto.response.RegisterResponse;
import com.zcomini.backend.auth.dto.response.UserResponse;
import com.zcomini.backend.auth.entity.RefreshTokenEntity;
import com.zcomini.backend.auth.entity.UserEntity;
import com.zcomini.backend.auth.mapper.TokenMapper;
import com.zcomini.backend.auth.mapper.UserMapper;
import com.zcomini.backend.auth.message.AuthMessage;
import com.zcomini.backend.auth.repository.RefreshTokenRepository;
import com.zcomini.backend.auth.repository.UserRepository;
import com.zcomini.backend.auth.security.AuthenticatedUser;
import com.zcomini.backend.auth.utils.AuthString;
import com.zcomini.backend.auth.utils.AuthUtils;
import com.zcomini.backend.auth.utils.TokenUtils;
import com.zcomini.backend.auth.validate.AuthValidator;
import com.zcomini.backend.auth.validate.TokenValidator;
import com.zcomini.backend.shared.api.dto.MessageResponse;
import com.zcomini.backend.shared.util.HashUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthUtils authUtils;
    private final TokenUtils tokenUtils;

    private final JwtService jwtService;

    private final UserMapper userMapper;
    private final TokenMapper tokenMapper;

    private final AuthValidator authValidator;
    private final TokenValidator tokenValidator;
    private final AuthMessage authMessage;

    @Transactional
    public RegisterResponse register(RegisterRequest request, String ipAddress) {
        authValidator.registerValidation(request);
        UserEntity user = userMapper.toRegister(request, passwordEncoder.encode(request.password()), "user");
        userRepository.save(user);
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.saveRefresh(user);
        authMessage.sendUserCreatedMessage(user, request.email());
        return RegisterResponse.from(accessToken, refreshToken, user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        UserEntity user = authUtils.findUserByEmail(request.email());
        authValidator.checkUserInactive(user);
        authValidator.checkPasswordMatch(request.password(), user.getPasswordHash());
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.saveRefresh(user);
        return LoginResponse.from(accessToken, refreshToken, user);
    }

    @Transactional
    public RefreshResponse refresh(String tokenValue, String ipAddress) {
        RefreshTokenEntity token = tokenUtils.findByTokenHash(HashUtils.sha256Hex(tokenValue));
        tokenValidator.checkRefreshTokenValid(token);
        token.setRevokedAt(OffsetDateTime.now());
        UserEntity user = token.getUser();
        authValidator.checkUserInactive(user);
        String nextRefreshToken = jwtService.saveRefresh(user);
        return RefreshResponse.from(jwtService.createAccessToken(user), nextRefreshToken);
    }

    @Transactional
    public MessageResponse logout(String refreshToken, String accessToken) {
        if (StringUtils.hasText(refreshToken)) {
            tokenUtils.revokeRefreshTokenWhenLogout(refreshToken);
        }
        tokenUtils.revokeAccessToken(accessToken);
        return new MessageResponse(AuthString.LOGOUT_RESPONSE);
    }

    @Transactional
    public MessageResponse logoutAll(AuthenticatedUser principal) {
        List<RefreshTokenEntity> tokens = refreshTokenRepository.findByUser_IdAndRevokedAtIsNull(principal.userId());
        tokenMapper.toRevokeTokens(tokens);
        tokenUtils.revokeUserAccessToken(principal.userId());
        return new MessageResponse(AuthString.LOGOUT_ALL_RESPONSE);
    }

    public UserResponse getMe(AuthenticatedUser principal) {
        UserEntity user = authUtils.findUserById(principal.userId());
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMe(AuthenticatedUser principal, UserRequest request, String ipAddress) {
        UserEntity user = authUtils.findUserById(principal.userId());
        userMapper.toUpdateFieldNeeded(user, request);
        return getMe(principal);
    }

    @Transactional
    public MessageResponse changePassword(AuthenticatedUser principal, ChangePasswordRequest request) {
        authValidator.checkNewPasswordDifferentFromOldPassword(request.oldPassword(), request.newPassword());
        UserEntity user = authUtils.findUserById(principal.userId());
        authValidator.checkPasswordMatch(request.oldPassword(), user.getPasswordHash());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return new MessageResponse(AuthString.CHANGE_PASSWORD_RESPONSE);
    }

    @Transactional
    public MessageResponse deactivateAccount(AuthenticatedUser principal, DeactivateRequest request,
            String tokenValue) {
        UserEntity user = authUtils.findUserById(principal.userId());
        authValidator.checkPasswordMatch(request.password(), user.getPasswordHash());
        userMapper.toDeactive(user, request);
        List<RefreshTokenEntity> tokens = refreshTokenRepository.findByUser_IdAndRevokedAtIsNull(user.getId());
        tokenMapper.toRevokeTokens(tokens);
        tokenUtils.revokeUserAccessToken(principal.userId());
        return new MessageResponse(AuthString.DEACTIVE_RESPONSE);
    }
}

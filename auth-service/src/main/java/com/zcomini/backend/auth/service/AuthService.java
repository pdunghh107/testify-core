package com.zcomini.backend.auth.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
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
import com.zcomini.backend.auth.exception.AuthException;
import com.zcomini.backend.auth.mapper.UserMapper;
import com.zcomini.backend.auth.message.AuthMessage;
import com.zcomini.backend.auth.repository.RefreshTokenRepository;
import com.zcomini.backend.auth.repository.UserRepository;
import com.zcomini.backend.auth.security.AuthenticatedUser;
import com.zcomini.backend.auth.validate.AuthValidator;
import com.zcomini.backend.shared.api.dto.MessageResponse;
import com.zcomini.backend.shared.util.HashUtils;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    // 3. Validate
    private final AuthValidator authValidator;
    // 4. Message
    private final AuthMessage authMessage;

    @Transactional
    public RegisterResponse register(RegisterRequest request, String ipAddress) {
        // 1. Check validate
        authValidator.registerValidation(request);
        // 2. Mapping dữ liệu
        UserEntity user = userMapper.toRegister(request, passwordEncoder.encode(request.password()), "user");
        // 3. Save dữ liệu
        userRepository.save(user);
        // 4. Tạo và lưu token
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.saveRefresh(user);
        // 5. Gửi sự kiện [người dùng đăng ký]
        authMessage.sendUserCreatedMessage(user, request.email());
        // 6. Return dữ liệu
        return RegisterResponse.from(accessToken, refreshToken, user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(AuthException::credentialsInvalid);
        if (!user.isActive()) {
            throw AuthException.userInactive();
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw AuthException.credentialsInvalid();
        }
        user.setLastLoginAt(OffsetDateTime.now());
        // 4. Tạo và lưu token
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.saveRefresh(user);
        return LoginResponse.from(accessToken, refreshToken, user);
    }

    @Transactional
    public RefreshResponse refresh(String tokenValue, String ipAddress) {
        RefreshTokenEntity token = refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(tokenValue))
                .orElseThrow(AuthException::refreshTokenInvalid);

        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw AuthException.refreshTokenInvalid();
        }

        token.setRevokedAt(OffsetDateTime.now());
        UserEntity user = token.getUser();
        if (!user.isActive()) {
            throw AuthException.userInactive();
        }

        String nextRefreshToken = jwtService.saveRefresh(user);

        return RefreshResponse.from(jwtService.createAccessToken(user), nextRefreshToken);
    }

    @Transactional
    public MessageResponse logout(String refreshToken, String accessToken) {
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(refreshToken))
                    .ifPresent(token -> {
                        token.setRevokedAt(OffsetDateTime.now());
                        refreshTokenRepository.save(token);
                    });
        }

        revokeAccessToken(accessToken);

        return new MessageResponse("Đăng xuất thành công");
    }

    @Transactional
    public MessageResponse logoutAll(AuthenticatedUser principal) {
        List<RefreshTokenEntity> tokens = refreshTokenRepository.findByUser_IdAndRevokedAtIsNull(principal.userId());
        for (RefreshTokenEntity token : tokens) {
            token.setRevokedAt(OffsetDateTime.now());
        }
        refreshTokenRepository.saveAll(tokens);

        revokeUserAccessToken(principal.userId());

        return new MessageResponse("Đăng xuất tất cả thiết bị thành công");
    }

    public UserResponse getMe(AuthenticatedUser principal) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(AuthException::userNotFound);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMe(AuthenticatedUser principal, UserRequest request, String ipAddress) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(AuthException::userNotFound);

        if (StringUtils.hasText(request.fullName())) {
            user.setFullName(request.fullName().trim());
        }
        if (StringUtils.hasText(request.phone())) {
            user.setPhone(request.phone().trim());
        }
        if (StringUtils.hasText(request.avatarUrl())) {
            user.setAvatarUrl(request.avatarUrl());
        }

        userRepository.save(user);
        return getMe(principal);
    }

    @Transactional
    public MessageResponse changePassword(AuthenticatedUser principal, ChangePasswordRequest request) {
        if (request.newPassword().equals(request.oldPassword())) {
            throw AuthException.newPasswordMustBeDifferent();
        }

        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(AuthException::userNotFound);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw AuthException.credentialsInvalid();
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new MessageResponse("Đổi mật khẩu thành công");
    }

    @Transactional
    public MessageResponse deactivateAccount(AuthenticatedUser principal, DeactivateRequest request,
            String tokenValue) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(AuthException::userNotFound);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw AuthException.credentialsInvalid();
        }

        user.setActive(false);
        user.setDeletedAt(OffsetDateTime.now());
        user.setDeletionReason("Deactivated by user");
        userRepository.save(user);

        // Revoke all refresh tokens
        List<RefreshTokenEntity> tokens = refreshTokenRepository.findByUser_IdAndRevokedAtIsNull(user.getId());
        for (RefreshTokenEntity token : tokens) {
            token.setRevokedAt(OffsetDateTime.now());
        }
        refreshTokenRepository.saveAll(tokens);

        revokeUserAccessToken(principal.userId());

        return new MessageResponse("Vô hiệu hóa tài khoản thành công");
    }

    private void revokeAccessToken(String accessToken) {
        try {
            Claims claims = jwtService.parse(accessToken);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            if (StringUtils.hasText(jti) && expiration != null) {
                long ttlMillis = expiration.getTime() - System.currentTimeMillis();
                if (ttlMillis > 0) {
                    stringRedisTemplate.opsForValue().set(
                            "token_revoked:" + jti,
                            "revoked",
                            ttlMillis,
                            TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception ex) {
        }
    }

    private void revokeUserAccessToken(UUID userId) {
        stringRedisTemplate.opsForValue().set(
                "user_revoked:" + userId.toString(),
                String.valueOf(Instant.now().toEpochMilli()), 30,
                TimeUnit.MINUTES);
    }
}

package com.zcomini.backend.auth.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
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
import com.zcomini.backend.auth.repository.RefreshTokenRepository;
import com.zcomini.backend.auth.repository.UserRepository;
import com.zcomini.backend.auth.security.AuthenticatedUser;
import com.zcomini.backend.shared.api.dto.MessageResponse;
import com.zcomini.backend.shared.event.UserRegisteredEvent;
import com.zcomini.backend.shared.util.HashUtils;
import com.zcomini.backend.shared.util.StringCustom;

import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service cốt lõi xử lý toàn bộ nghiệp vụ liên quan đến Xác thực
 * (Authentication)
 * và Quản lý tài khoản người dùng (Account Management).
 * <p>
 * Class này chịu trách nhiệm tương tác với cơ sở dữ liệu để đăng ký, đăng nhập,
 * cập nhật hồ sơ, quản lý chu kỳ sống của các phiên làm việc (Access/Refresh
 * Tokens),
 * và điều phối các sự kiện Message Broker (ví dụ: tạo Workspace mặc định khi
 * đăng ký).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.notification.rabbit.exchange:notification.events}")
    private String exchangeName;

    /**
     * Xử lý luồng đăng ký tài khoản mới cho người dùng.
     * <p>
     * Phương thức này thực hiện lưu thông tin người dùng vào cơ sở dữ liệu sau khi
     * mã hoá mật khẩu.
     * Sau khi đăng ký thành công, hệ thống sẽ tự động cấp phát Refresh Token và bắn
     * sự kiện
     * {@code user.registered} qua RabbitMQ để khởi tạo Workspace mặc định.
     *
     * @param request   Dữ liệu đăng ký do người dùng gửi lên, đảm bảo không bị
     *                  trùng lặp email.
     * @param ipAddress Địa chỉ IP của người dùng thực hiện yêu cầu (dùng để lưu
     *                  vết, bảo mật).
     * @return Thông tin tài khoản cùng với Access Token và Refresh Token mới.
     * @throws AuthException Nếu địa chỉ email trong {@code request} đã tồn tại
     *                       trong hệ thống hoặc mật khẩu xác nhận không khớp.
     * @see UserRegisteredEvent
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request, String ipAddress) {
        if (!request.password().equals(request.confirmPassword())) {
            throw AuthException.passwordMismatch();
        }
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw AuthException.emailTaken();
        }
        UserEntity user = userMapper.toRegister(request, passwordEncoder.encode(request.password()), "user");
        userRepository.save(user);
        String refreshToken = jwtService.saveRefresh(user);

        rabbitTemplate.convertAndSend(exchangeName, "user.registered",
                new UserRegisteredEvent(user.getId(), user.getEmail(),
                        StringCustom.extractUsernameFromEmail(request.email()) + "'s Workspace"));

        return RegisterResponse.from(jwtService.createAccessToken(user), refreshToken, user);
    }

    /**
     * Xác thực thông tin đăng nhập và cấp phát phiên làm việc mới (Tokens).
     * <p>
     * Dựa vào email và mật khẩu, hệ thống kiểm tra đối chiếu với cơ sở dữ liệu.
     * Nếu hợp lệ, thời gian đăng nhập cuối cùng (lastLoginAt) sẽ được cập nhật
     * và một cặp Access/Refresh Token mới được sinh ra để định danh phiên làm việc.
     *
     * @param request   Thông tin đăng nhập gồm email và mật khẩu dạng chuỗi gốc
     *                  (plain-text).
     * @param ipAddress Địa chỉ IP của người dùng đăng nhập.
     * @return Thông tin tài khoản kèm theo Access Token và Refresh Token hợp lệ.
     * @throws AuthException Nếu email không tồn tại, mật khẩu sai hoặc tài khoản
     *                       đang bị vô hiệu hoá.
     */
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
        String refreshToken = jwtService.saveRefresh(user);
        return LoginResponse.from(jwtService.createAccessToken(user), refreshToken, user);
    }

    /**
     * Cấp phát lại Access Token mới dựa trên Refresh Token hợp lệ.
     * <p>
     * Hàm này kiểm tra tính hợp lệ của Refresh Token (chưa hết hạn, chưa bị thu
     * hồi).
     * Nếu hợp lệ, Token cũ sẽ bị đánh dấu là đã thu hồi (revoked) và một cặp Token
     * mới
     * được tạo ra. Cơ chế xoay vòng này (Token Rotation) giúp tăng cường tính bảo
     * mật.
     *
     * @param tokenValue Chuỗi ký tự của Refresh Token hiện tại cần được gia hạn.
     * @param ipAddress  Địa chỉ IP yêu cầu gia hạn Token.
     * @return Cặp Access Token và Refresh Token mới để tiếp tục phiên làm việc.
     * @throws AuthException Nếu Refresh Token không hợp lệ, đã bị thu hồi, hết hạn
     *                       hoặc tài khoản bị khoá.
     */
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

    /**
     * Đăng xuất người dùng khỏi thiết bị hiện tại và thu hồi các token liên quan.
     * <p>
     * Quá trình này thực hiện hai việc:
     * 1. Tìm và đánh dấu {@code Refresh Token} là đã bị thu hồi trong cơ sở dữ liệu
     * để ngăn chặn việc cấp phát token mới.
     * 2. Đưa {@code Access Token} hiện tại vào danh sách đen (blacklist) trên Redis
     * dựa trên mã định danh JTI của nó.
     *
     * @param refreshToken Chuỗi Refresh Token cần thu hồi (có thể truyền
     *                     {@code null} nếu Client không đính kèm cookie).
     * @param accessToken  Chuỗi Access Token nguyên bản từ Header để trích xuất JTI
     *                     (có thể truyền {@code null}).
     * @return Thông báo trạng thái đăng xuất thành công.
     * @see #revokeAccessToken(String)
     */
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

    /**
     * Đăng xuất người dùng khỏi tất cả các thiết bị đã đăng nhập.
     * <p>
     * Phương thức này vô hiệu hóa triệt để toàn bộ phiên làm việc của người dùng
     * bằng cách:
     * 1. Thu hồi mọi {@code Refresh Token} đang hoạt động trong cơ sở dữ liệu.
     * 2. Thêm trực tiếp {@code userId} vào danh sách đen trên Redis để chặn mọi
     * {@code Access Token} còn hạn.
     * 
     * @param principal Đối tượng chứa thông tin xác thực của người dùng đang thực
     *                  hiện yêu cầu.
     * @return Thông báo trạng thái đăng xuất toàn cục thành công.
     * @see #revokeUserAccessToken(UUID)
     */
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

    /**
     * Truy xuất thông tin hồ sơ (Profile) của người dùng đang đăng nhập.
     * <p>
     * Dựa trên ID người dùng được trích xuất từ Access Token (thông qua đối tượng
     * {@code AuthenticatedUser}),
     * phương thức này sẽ truy vấn cơ sở dữ liệu để trả về thông tin chi tiết của
     * người dùng đó.
     *
     * @param principal Đối tượng chứa thông tin xác thực được trích xuất từ JWT.
     * @return Dữ liệu hồ sơ của người dùng (không bao gồm mật khẩu).
     * @throws AuthException Nếu không tìm thấy người dùng trong cơ sở dữ liệu.
     */
    public UserResponse getMe(AuthenticatedUser principal) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(AuthException::userNotFound);
        return UserResponse.from(user);
    }

    /**
     * Cập nhật thông tin cá nhân của người dùng.
     * <p>
     * Chỉ những trường dữ liệu có giá trị (không rỗng, không chứa toàn khoảng
     * trắng) mới được cập nhật.
     * Các thông tin hỗ trợ cập nhật bao gồm họ tên, số điện thoại và đường dẫn ảnh
     * đại diện.
     *
     * @param principal Đối tượng chứa thông tin xác thực của người dùng hiện tại.
     * @param request   Dữ liệu chứa các trường thông tin cần cập nhật.
     * @param ipAddress Địa chỉ IP của người dùng thực hiện yêu cầu.
     * @return Dữ liệu hồ sơ của người dùng sau khi đã được cập nhật thành công.
     * @throws AuthException Nếu không tìm thấy người dùng.
     */
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

    /**
     * Thay đổi mật khẩu của tài khoản.
     * <p>
     * Người dùng bắt buộc phải cung cấp mật khẩu cũ để xác thực trước khi đổi sang
     * mật khẩu mới.
     * Mật khẩu mới sẽ được mã hoá bằng thuật toán băm (hashing) trước khi lưu vào
     * cơ sở dữ liệu
     * để đảm bảo an toàn.
     *
     * @param principal Đối tượng chứa thông tin xác thực của người dùng hiện tại.
     * @param request   Dữ liệu chứa mật khẩu cũ và mật khẩu mới.
     * @return Thông báo xác nhận việc đổi mật khẩu thành công.
     * @throws AuthException Nếu người dùng không tồn tại, mật khẩu cũ không
     *                       khớp, hoặc mật khẩu mới trùng với mật khẩu cũ.
     */
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

    /**
     * Vô hiệu hoá (xoá mềm) tài khoản người dùng khỏi hệ thống.
     * <p>
     * Hành động này yêu cầu người dùng phải xác nhận bằng mật khẩu hiện tại. Khi
     * thành công,
     * tài khoản sẽ bị chuyển sang trạng thái ngưng hoạt động (inactive).
     * Hơn nữa, toàn bộ Refresh Token của người dùng này sẽ bị thu hồi và ID của
     * người dùng
     * sẽ được đẩy vào Blacklist trên Redis để vô hiệu hoá tức thì các Access Token
     * đang còn hạn.
     *
     * @param principal  Đối tượng chứa thông tin xác thực của người dùng hiện tại.
     * @param request    Dữ liệu chứa mật khẩu xác nhận việc vô hiệu hoá.
     * @param tokenValue Refresh Token hiện tại (nếu có).
     * @return Thông báo xác nhận tài khoản đã được vô hiệu hoá thành công.
     * @throws AuthException Nếu người dùng không tồn tại hoặc mật khẩu xác nhận
     *                       không khớp.
     */
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

    /**
     * Đưa một Access Token cụ thể vào danh sách đen (Blacklist) trên Redis thông
     * qua mã định danh JTI.
     * <p>
     * Hàm này sẽ tự động giải mã JWT để lấy ra {@code jti} và {@code exp} (thời
     * gian hết hạn).
     * Nó tính toán thời gian sống còn lại (TTL) và lưu trữ key
     * {@code token_revoked:{jti}}
     * trên Redis đúng bằng khoảng thời gian TTL đó nhằm tối ưu bộ nhớ.
     * Nếu token đã hết hạn hoặc không hợp lệ, hàm sẽ chủ động bỏ qua lỗi thay vì
     * làm gián đoạn luồng thực thi.
     *
     * @param accessToken Chuỗi Access Token gốc cần thu hồi. Nếu truyền
     *                    {@code null} hoặc chuỗi rỗng, hàm sẽ tự động kết thúc.
     */
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

    /**
     * Đưa toàn bộ phiên làm việc của một người dùng vào danh sách đen (Blacklist)
     * trên Redis.
     * <p>
     * Bằng cách lưu key {@code user_revoked:{userId}} kèm theo thời điểm thu hồi,
     * mọi Access Token được phát hành trước thời điểm này sẽ bị
     * {@link com.zcomini.backend.auth.security.JwtAuthenticationFilter}
     * từ chối. Thời gian tồn tại (TTL) của key được thiết lập mặc định (VD: 30
     * phút) bằng đúng cấu hình vòng đời của một Access Token.
     *
     * @param userId Mã định danh duy nhất (UUID) của người dùng cần thu hồi quyền
     *               truy cập.
     */
    private void revokeUserAccessToken(UUID userId) {
        stringRedisTemplate.opsForValue().set(
                "user_revoked:" + userId.toString(),
                String.valueOf(Instant.now().toEpochMilli()), 30,
                TimeUnit.MINUTES);
    }
}

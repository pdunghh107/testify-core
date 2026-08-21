package com.zcomini.backend.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.zcomini.backend.auth.config.AuthProperties;
import com.zcomini.backend.auth.entity.RefreshTokenEntity;
import com.zcomini.backend.auth.entity.UserEntity;
import com.zcomini.backend.auth.repository.RefreshTokenRepository;
import com.zcomini.backend.shared.util.HashUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Dịch vụ xử lý các nghiệp vụ liên quan đến JSON Web Token (JWT).
 * <p>
 * Lớp này chịu trách nhiệm khởi tạo, ký (sign) và xác thực (verify) các token.
 * Nó giao tiếp trực tiếp với {@link AuthProperties} để lấy các cấu hình bí mật
 * và thiết lập thời gian sống của token.
 */
@Service
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;
    private final SecretKey secretKey;

    /**
     * Khởi tạo dịch vụ JWT và tính toán khóa bí mật (Secret Key).
     * <p>
     * Khóa bí mật được tự động tạo ra từ chuỗi {@code jwtSecret} trong cấu hình ứng dụng,
     * sử dụng thuật toán HMAC-SHA để đảm bảo an toàn tuyệt đối cho việc ký token.
     *
     * @param authProperties         Cấu hình chứa khóa bí mật và thời hạn sinh token.
     * @param refreshTokenRepository Repository để tương tác với bảng lưu trữ Refresh Token.
     */
    public JwtService(AuthProperties authProperties, RefreshTokenRepository refreshTokenRepository) {
        this.authProperties = authProperties;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Tạo một Access Token mới định danh cho phiên làm việc của người dùng.
     * <p>
     * Token này chứa các thông tin (claims) cơ bản như ID, Email và Role.
     * Thời gian sống của token được thiết lập ngắn (nhằm giảm thiểu rủi ro bảo mật nếu lộ token),
     * tuân thủ nghiêm ngặt theo cấu hình {@code accessTokenMinutes} trong hệ thống.
     *
     * @param user Đối tượng chứa thông tin người dùng cần cấp phát token, không được {@code null}.
     * @return Chuỗi JWT (Access Token) đã được ký điện tử an toàn, sẵn sàng trả về Client.
     */
    public String createAccessToken(UserEntity user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(authProperties.jwtIssuer())
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(authProperties.accessTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Giải mã và xác thực tính hợp lệ của một chuỗi JWT.
     * <p>
     * Phương thức này kiểm tra xem chữ ký của token có khớp với khóa bí mật hay không (chống giả mạo),
     * và token đã hết hạn hay chưa. Nếu token không hợp lệ, thư viện JJWT sẽ tự động
     * ném ra các ngoại lệ tương ứng.
     *
     * @param token Chuỗi Access Token cần giải mã (thường lấy từ Header Authorization).
     * @return Đối tượng {@link Claims} chứa toàn bộ thông tin (payload) bên trong token.
     * @throws io.jsonwebtoken.JwtException Nếu token bị sai chữ ký, đã hết hạn, hoặc có định dạng không hợp lệ.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Khởi tạo, băm (hash) và lưu trữ một Refresh Token vào cơ sở dữ liệu.
     * <p>
     * Token được sinh ngẫu nhiên dạng UUID. Để phòng chống rủi ro lộ lọt dữ liệu (Database Leak),
     * chuỗi token sẽ được băm bằng thuật toán SHA-256 thông qua {@link HashUtils} trước khi lưu.
     * Thời hạn của Token tự động lấy từ cấu hình {@code refreshTokenDays}.
     *
     * @param user Thực thể người dùng sở hữu Refresh Token này, không được {@code null}.
     * @return Chuỗi Refresh Token gốc dạng ký tự thuần (chưa băm) để gửi trả về cho Client.
     */
    public String saveRefresh(UserEntity user) {
        String token = UUID.randomUUID().toString();
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(HashUtils.sha256Hex(token));
        entity.setExpiresAt(OffsetDateTime.now().plusDays(authProperties.refreshTokenDays()));
        refreshTokenRepository.save(entity);
        return token;
    }
}

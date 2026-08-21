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

/**
 * REST Controller cung cấp các API công khai và bảo mật liên quan đến Xác thực
 * (Authentication)
 * và Quản lý tài khoản người dùng (Account Management).
 * <p>
 * Controller này đảm nhận việc giao tiếp với Frontend/Client, xử lý Cookie cho
 * Refresh Token
 * và gọi xuống tầng {@link AuthService} để thực thi nghiệp vụ cốt lõi.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Đăng ký tài khoản người dùng mới.
     * <p>
     * Trả về thông tin user kèm JWT Access Token trong body.
     * Đồng thời tự động thiết lập Cookie chứa Refresh Token ở chế độ HttpOnly.
     *
     * @param request      Thông tin đăng ký (email, mật khẩu, họ tên, số điện
     *                     thoại).
     * @param httpRequest  Đối tượng HTTP Request để lấy IP.
     * @param httpResponse Đối tượng HTTP Response để đính kèm Cookie.
     * @return Dữ liệu tài khoản vừa tạo.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        RegisterResponse response = authService.register(request, httpRequest.getRemoteAddr());
        setRefreshTokenCookie(httpResponse, response.refreshToken());
        return response;
    }

    /**
     * Đăng nhập vào hệ thống bằng Email và Mật khẩu.
     * <p>
     * Nếu xác thực thành công, trả về Access Token trong body và gắn Refresh Token
     * vào Cookie (HttpOnly) nhằm đảm bảo an toàn, chống tấn công XSS.
     *
     * @param request      Thông tin đăng nhập.
     * @param httpRequest  Đối tượng HTTP Request để lấy IP.
     * @param httpResponse Đối tượng HTTP Response để đính kèm Cookie.
     * @return Dữ liệu tài khoản cùng Access Token.
     */
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request, httpRequest.getRemoteAddr());
        setRefreshTokenCookie(httpResponse, response.refreshToken());
        return response;
    }

    /**
     * Làm mới (Refresh) phiên làm việc bằng Refresh Token lưu trong Cookie.
     * <p>
     * Endpoint này sẽ đọc giá trị {@code refreshToken} từ Cookie. Nếu Token còn
     * hạn,
     * hệ thống sẽ xoay vòng (cấp Token mới) và thay thế vào Cookie cũ.
     *
     * @param refreshToken Chuỗi Token lấy tự động từ Cookie.
     * @param httpRequest  Đối tượng HTTP Request để lấy IP.
     * @param httpResponse Đối tượng HTTP Response để cập nhật lại Cookie.
     * @return Access Token mới để tiếp tục gọi API.
     */
    @PostMapping("/refresh")
    public RefreshResponse refresh(
            @CookieValue String refreshToken, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        RefreshResponse response = authService.refresh(refreshToken, httpRequest.getRemoteAddr());
        setRefreshTokenCookie(httpResponse, response.refreshToken());
        return response;
    }

    /**
     * Chấm dứt phiên đăng nhập hiện tại trên thiết bị đang sử dụng.
     * <p>
     * API này thực hiện các bước bảo mật sau để đảm bảo đăng xuất an toàn:
     * 1. Xóa bỏ Cookie chứa {@code Refresh Token} ở phía trình duyệt (Client).
     * 2. Gọi Service để vô hiệu hóa vĩnh viễn {@code Refresh Token} này trong Database.
     * 3. Trích xuất {@code Access Token} từ Header để đưa vào danh sách đen (Blacklist) trên Redis, 
     *    ngăn chặn việc token bị đánh cắp và sử dụng lại.
     *
     * @param refreshToken Chuỗi Refresh Token tự động được trích xuất từ Cookie (có thể {@code null}).
     * @param authHeader   Chuỗi Header chứa Access Token (định dạng {@code Bearer ...}), dùng để cấm cửa token hiện hành.
     * @param httpResponse Đối tượng phản hồi HTTP, được sử dụng để tiêm lệnh xóa Cookie vào Browser.
     * @return Thông báo xác nhận quá trình đăng xuất hoàn tất.
     * @see com.zcomini.backend.auth.service.AuthService#logout(String, String)
     */
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

    /**
     * Hủy bỏ toàn bộ phiên đăng nhập của người dùng trên mọi thiết bị.
     * <p>
     * API này giải quyết trường hợp nghi ngờ tài khoản bị xâm phạm hoặc người dùng muốn đăng xuất khỏi tất cả các nơi.
     * Nó kích hoạt cấm cửa toàn cục (Global Revocation) bằng cách vô hiệu hóa mọi {@code Refresh Token} và 
     * đưa định danh người dùng vào sổ đen Redis. Trình duyệt hiện tại cũng sẽ tự động bị xóa Cookie.
     *
     * @param authentication Đối tượng chứa thông tin Profile người dùng do Spring Security tự động tiêm vào (trích xuất từ JWT).
     * @param httpResponse   Đối tượng phản hồi HTTP, được sử dụng để dọn dẹp Cookie ở thiết bị gọi API này.
     * @return Thông báo xác nhận tài khoản đã được đăng xuất khỏi toàn bộ hệ thống.
     * @see com.zcomini.backend.auth.service.AuthService#logoutAll(com.zcomini.backend.auth.security.AuthenticatedUser)
     */
    @PostMapping("/logout-all")
    public MessageResponse logoutAll(
            Authentication authentication,
            HttpServletResponse httpResponse) {
        MessageResponse msg = authService.logoutAll((AuthenticatedUser) authentication.getPrincipal());
        clearRefreshTokenCookie(httpResponse);
        return msg;
    }

    /**
     * Lấy thông tin chi tiết của người dùng đang đăng nhập.
     *
     * @param authentication Đối tượng chứa thông tin xác thực do Spring Security
     *                       quản lý.
     * @return Toàn bộ hồ sơ cá nhân của User hiện tại.
     */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authService.getMe((AuthenticatedUser) authentication.getPrincipal());
    }

    /**
     * Cập nhật thông tin cá nhân (Profile) của người dùng hiện tại.
     *
     * @param authentication Đối tượng chứa thông tin xác thực.
     * @param request        Các trường thông tin cần cập nhật (họ tên, SĐT, ảnh đại
     *                       diện).
     * @param httpRequest    Đối tượng HTTP Request để lấy IP.
     * @return Thông tin người dùng sau khi đã cập nhật thành công.
     */
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

    /**
     * Đổi mật khẩu tài khoản đang đăng nhập.
     *
     * @param authentication Đối tượng chứa thông tin xác thực.
     * @param request        Mật khẩu cũ và mật khẩu mới.
     * @return Thông báo đổi mật khẩu thành công.
     */
    @PostMapping("/me/password")
    public MessageResponse changePassword(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse msg = authService.changePassword((AuthenticatedUser) authentication.getPrincipal(), request);
        return msg;
    }

    /**
     * Vô hiệu hóa (Deactivate) tài khoản hiện tại.
     * <p>
     * Sau khi vô hiệu hóa, tài khoản sẽ không thể đăng nhập.
     * Tất cả các Token (Access/Refresh) đang tồn tại đều bị thu hồi lập tức.
     *
     * @param authentication Đối tượng chứa thông tin xác thực.
     * @param request        Xác nhận bằng mật khẩu hiện tại.
     * @param refreshToken   Refresh Token lấy từ Cookie để thu hồi.
     * @param httpResponse   Đối tượng HTTP Response để xóa bỏ Cookie.
     * @return Thông báo vô hiệu hóa thành công.
     */
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

    /**
     * Thiết lập cookie chứa Refresh Token an toàn vào đối tượng HTTP Response.
     * <p>
     * Cookie được cấu hình với các cờ bảo mật nghiêm ngặt như {@code HttpOnly}
     * (chống XSS),
     * {@code Secure} (chỉ truyền qua HTTPS) và {@code SameSite=Strict} (chống
     * CSRF).
     * Thời gian sống mặc định của cookie này là 30 ngày (30 * 24 * 60 * 60 giây).
     *
     * @param response     Đối tượng phản hồi HTTP sẽ được gắn thêm Header
     *                     Set-Cookie, không được {@code null}.
     * @param refreshToken Chuỗi Refresh Token cần lưu trữ, không được {@code null}
     *                     hoặc rỗng.
     */
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

    /**
     * Xóa cookie chứa Refresh Token khỏi trình duyệt của người dùng.
     * <p>
     * Phương thức này thực hiện bằng cách ghi đè lên cookie cũ một giá trị rỗng
     * và đặt thời gian sống (Max-Age) về 0, buộc trình duyệt phải xóa cookie này
     * ngay lập tức.
     * Chức năng này thường được gọi khi người dùng đăng xuất hoặc khi tài khoản bị
     * vô hiệu hoá.
     *
     * @param response Đối tượng phản hồi HTTP sẽ được gắn lệnh xóa cookie, không
     *                 được {@code null}.
     */
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

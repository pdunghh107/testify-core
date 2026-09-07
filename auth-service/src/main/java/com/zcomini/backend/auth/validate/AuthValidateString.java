package com.zcomini.backend.auth.validate;

public final class AuthValidateString {

    private AuthValidateString() {
    }

    public static final String EMAIL_REQUIRED = "Vui lòng nhập email";
    public static final String EMAIL_INVALID = "Email không hợp lệ";

    public static final String PASSWORD_REQUIRED = "Vui lòng nhập mật khẩu";
    public static final String PASSWORD_INVALID = "Mật khẩu không hợp lệ";
    public static final String OLD_PASSWORD_REQUIRED = "Vui lòng nhập mật khẩu cũ";
    public static final String NEW_PASSWORD_REQUIRED = "Vui lòng nhập mật khẩu mới";
    public static final String CONFIRM_PASSWORD_REQUIRED = "Vui lòng nhập lại mật khẩu";

    public static final String FULL_NAME_REQUIRED = "Vui lòng nhập họ và tên";
    public static final String FULL_NAME_INVALID = "Họ và tên không hợp lệ";

    public static final String PHONE_REQUIRED = "Vui lòng nhập số điện thoại";

    public static final String DELETED_REASON_MAX_LENGTH = "Lý do không được vượt quá 255 ký tự";
}

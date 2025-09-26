package com.pm.authservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOTFOUND(404, "Email không tồn tại"),
    UNCATEGORIZED_ERROR(1000, "Uncategorized error"),
    INVALID_KEY(1001, "Invalid key"),
    EMAIL_ALREADY_EXISTS(400, "Email đã tồn tại"),
    PASSWORD_NOT_MATCH(400, "Mật khẩu không khớp"),
    ROLE_NOTFOUND(404, "Role not found"),
    UNAUTHENTICATED(1005, "Unauthenticated"),
    REFRESHTOKEN_INVALID(404, "Refresh token không tồn tại"),
    REFRESHTOKEN_ISREVOKED(401, "Refresh token is overdue"),
    PASSWORD_INVALID(400, "Mật khẩu phải trên 6 ký tự"),
    EMAIL_INVALID(400, "Email không hợp lệ"),
    NOT_BLANK(400, "Không được để trống phần này"),
    PASSWORD_NOT_TRUE(400, "Mật khẩu không đúng"),
    CANT_CONNECT_USERSERVICE(500, "Can't connect user-service"),
    CANT_CHANGE_ROLE(400, "Không thể đổi role thành USER"),
    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

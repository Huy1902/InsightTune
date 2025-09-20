package com.pm.authservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOTFOUND(404, "Email không tồn tại"),
    UNCATEGORIZED_ERROR(1000, "Uncategorized error"),
    INVALID_KEY(1001, "Invalid key"),
    EMAIL_ALREADY_EXISTS(1002, "Email đã tồn tại"),
    PASSWORD_NOT_MATCH(1003, "Mật khẩu không khớp"),
    ROLE_NOTFOUND(404, "Role not found"),
    UNAUTHENTICATED(1005, "Unauthenticated"),
    REFRESHTOKEN_INVALID(1006, "Refresh token invalid"),
    REFRESHTOKEN_ISREVOKED(1007, "Refresh token is overdue"),
    PASSWORD_INVALID(1008, "Mật khẩu phải trên 6 ký tự"),
    EMAIL_INVALID(1009, "Email không hợp lệ"),
    NOT_BLANK(1011, "Không được để trống phần này"),
    PASSWORD_NOT_TRUE(1004, "Mật khẩu không đúng"),
    CANT_CONNECT_USERSERVICE(1010, "Can't connect userservice"),
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

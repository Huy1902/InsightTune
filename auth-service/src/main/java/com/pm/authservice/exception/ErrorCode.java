package com.pm.authservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOTFOUND(404, "User not found"),
    UNCATEGORIZED_ERROR(1000, "Uncategorized error"),
    INVALID_KEY(1001, "Invalid key"),
    EMAIL_ALREADY_EXISTS(1002, "Email already exists"),
    PASSWORD_NOT_MATCH(1003, "Password not match"),
    ROLE_NOTFOUND(1004, "Role not found"),
    UNAUTHENTICATED(1005, "Unauthenticated"),
    REFRESHTOKEN_INVALID(1006, "Refresh token invalid"),
    REFRESHTOKEN_ISREVOKED(1007, "Refresh token is overdue"),
    PASSWORD_INVALID(1008, "Password invalid"),
    EMAIL_INVALID(1009, "Email invalid"),
    USERNAME_INVALID(10010, "Username must be more than 3 letters"),
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

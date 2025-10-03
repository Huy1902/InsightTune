package com.pm.authservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOTFOUND(404, "Email not found"),
    UNCATEGORIZED_ERROR(1000, "Uncategorized error"),
    INVALID_KEY(1001, "Invalid key"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),
    PASSWORD_NOT_MATCH(400, "Passwords do not match"),
    ROLE_NOTFOUND(404, "Role not found"),
    UNAUTHENTICATED(1005, "Unauthenticated"),
    REFRESHTOKEN_INVALID(404, "Refresh token not found"),
    REFRESHTOKEN_ISREVOKED(401, "Refresh token is revoked"),
    PASSWORD_INVALID(400, "Password must be at least 6 characters"),
    EMAIL_INVALID(400, "Invalid email"),
    NOT_BLANK(400, "Field must not be blank"),
    PASSWORD_NOT_TRUE(400, "Incorrect password"),
    CANT_CONNECT_USERSERVICE(500, "Cannot connect to user-service"),
    CANT_CHANGE_ROLE(400, "Cannot change role to USER"),
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

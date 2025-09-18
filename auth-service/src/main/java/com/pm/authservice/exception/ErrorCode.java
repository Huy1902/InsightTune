package com.pm.authservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOTFOUND(404, "User not found"),
    UNCATEGORIZED_ERROR(1000, "Uncategorized error"),
    INVALID_KEY(1001, "Invalid key"),
    EMAIL_ALREADY_EXISTS(1002, "Email already exists"),
    PASSWORD_NOT_MATCH(1003, "Password not match"),
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

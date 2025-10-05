package com.pm.userservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOTFOUND(404, "User not found"),
    UNCATEGORIZED_ERROR(999, "Uncategorized error"),
    INVALID_KEY(1008, "Invalid key"),
    NOT_BLANK(400, "This field cannot be empty"),
    CANT_CONNECT_AUTHSERVICE(500, "Cannot connect to auth-service"),
    CANT_CHANGE_ROLE(400, "Cannot change role to USER"),
    CANT_UPLOAD_AVATAR(400, "Cannot upload file to cloud"),
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

package com.pm.userservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOTFOUND(404, "User not found"),
    UNCATEGORIZED_ERROR(999, "Uncategorized error"),
    INVALID_KEY(1008, "Invalid key"),
    NOT_BLANK(400, "Không được để trống phần này"),
    CANT_CONNECT_AUTHSERVICE(500, "Cant connect auth-service"),
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

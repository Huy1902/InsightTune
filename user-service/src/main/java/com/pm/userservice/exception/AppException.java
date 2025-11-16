package com.pm.userservice.exception;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppException extends RuntimeException{
    private ErrorCode error;

    public AppException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }

}

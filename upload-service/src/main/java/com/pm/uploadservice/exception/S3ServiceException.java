package com.pm.uploadservice.exception;

public class S3ServiceException extends RuntimeException {
  public S3ServiceException(String message) {
    super(message);
  }
}

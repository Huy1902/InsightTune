package com.pm.uploadservice.exception;

public class KafkaServiceException extends RuntimeException {
  public KafkaServiceException(String message) {
    super(message);
  }
}

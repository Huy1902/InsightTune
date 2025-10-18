package com.pm.catalogservice.exception;

public class KafkaServiceException extends RuntimeException {
    public KafkaServiceException(String message) {
        super(message);
    }
}

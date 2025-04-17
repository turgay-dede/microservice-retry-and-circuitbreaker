package com.turgaydede.exception;

import org.springframework.http.HttpStatus;

public class OrderProcessingException extends RuntimeException {

    private final HttpStatus status;

    public OrderProcessingException(String message) {
        super(message);
        this.status = HttpStatus.SERVICE_UNAVAILABLE;
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.SERVICE_UNAVAILABLE;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

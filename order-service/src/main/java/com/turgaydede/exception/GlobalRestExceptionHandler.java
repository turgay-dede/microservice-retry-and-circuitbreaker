package com.turgaydede.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler(OrderProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleOrderError(OrderProcessingException ex, HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", System.currentTimeMillis());
        error.put("path", request.getRequestURI());
        error.put("status", ex.getStatus().value());
        error.put("error", ex.getStatus().getReasonPhrase());
        error.put("message", ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(error);
    }
}


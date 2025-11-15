package com.microservice.order_service.common.exceptions;

import com.microservice.order_service.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1️⃣ Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse("Validation failed", errors, HttpStatus.BAD_REQUEST);
    }

    // 2️⃣ Runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime exception: {}", ex.getMessage());
        String cleanMessage = cleanExceptionMessage(ex.getMessage());

        return buildResponse("Something went wrong", cleanMessage, HttpStatus.BAD_REQUEST);
    }

    // 3️⃣ Fallback for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        String cleanMessage = cleanExceptionMessage(ex.getMessage());

        return buildResponse("Internal server error", cleanMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 🔹 Reusable method to build ApiResponse
    private ResponseEntity<ApiResponse<?>> buildResponse(String message, Object errorDetails, HttpStatus status) {
        Map<String, Object> data = Map.of(
                "status", status.value(),
                "error", errorDetails,
                "timestamp", Instant.now().toString()
        );

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(message, data, false);
        return ResponseEntity.status(status).body(response);
    }

    // 🔹 Optional: clean exception messages
    private String cleanExceptionMessage(String message) {
        if (message == null) return "Unexpected error occurred";
        return message.replaceAll("^\\d{3}\\s[A-Z]+\\s*\"?|\"?$", "").trim();
    }
}
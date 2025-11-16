package com.microservice.order_service.common.exceptions;

import com.microservice.order_service.common.dto.ApiResponse;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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

    // 1️⃣ Handle @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        log.warn("Validation failed: {}", errors);

        return buildResponse(
                "Validation failed",
                errors,
                HttpStatus.BAD_REQUEST
        );
    }

    // 2️⃣ Handle business logic errors safely
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime exception: {}", ex.getMessage());

        return buildResponse(
                "Something went wrong",
                safeMessage(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    // 3️⃣ Handle Feign client errors (User/Product service failures)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<?>> handleFeignException(FeignException ex) {

        log.error("Feign client error: status={}, msg={}", ex.status(), ex.getMessage());

        String cleaned = extractFeignMessage(ex);

        HttpStatus status = ex.status() > 0 ?
                HttpStatus.valueOf(ex.status()) :
                HttpStatus.SERVICE_UNAVAILABLE;

        return buildResponse(
                "External service communication failed",
                cleaned,
                status
        );
    }

    // 4️⃣ Catch-all handler for unknown exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);

        return buildResponse(
                "Internal server error",
                safeMessage(ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // 🔹 Utility: API Response builder
    private ResponseEntity<ApiResponse<?>> buildResponse(String message, Object error, HttpStatus status) {

        ErrorDetail detail = new ErrorDetail(
                Instant.now().toString(),
                status.value(),
                error
        );

        ApiResponse<ErrorDetail> response = new ApiResponse<>(
                message,
                detail,
                false
        );

        return ResponseEntity.status(status).body(response);
    }

    // 🔹 Clean internal exception messages
    private String safeMessage(String msg) {
        if (msg == null) return "Unexpected error occurred";
        return msg.replaceAll("[\"]", "").trim();
    }

    // 🔹 Extract Feign clean message
    private String extractFeignMessage(FeignException ex) {
        String msg = ex.contentUTF8();
        if (msg == null || msg.isEmpty()) {
            return "External service unavailable";
        }
        return msg.replaceAll("\"", "").trim();
    }

    // 🔹 Response payload for errors
    @Getter
    @Setter
    @AllArgsConstructor
    static class ErrorDetail {
        private String timestamp;
        private int status;
        private Object error;
    }
}
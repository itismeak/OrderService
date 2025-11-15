package com.microservice.order_service.common.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String message;
    private T data;
    private boolean status;
    public ApiResponse(String message, T data, boolean status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }
    // Optional helper constructors for success/error
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, true);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, false);
    }
}
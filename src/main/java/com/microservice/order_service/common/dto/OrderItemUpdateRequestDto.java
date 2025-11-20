package com.microservice.order_service.common.dto;

import com.microservice.order_service.common.enums.OrderItemStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemUpdateRequestDto {
    @NotNull(message = "OrderItem ID is required")
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 100, message = "Product name cannot exceed 100 characters")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Positive(message = "Quantity must be greater than zero")
    private int quantity;

    private OrderItemStatus status;

}
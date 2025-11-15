package com.microservice.order_service.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemViewDto {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
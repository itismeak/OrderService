package com.microservice.order_service.common.dto;

import com.microservice.order_service.common.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderViewDto {
    private Long id;
    private String orderCode;
    private UserDto user;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemViewDto> items;
}
package com.microservice.order_service.module.order.service;

import com.microservice.order_service.common.dto.OrderRequestDto;
import com.microservice.order_service.common.dto.OrderViewDto;
import com.microservice.order_service.common.enums.OrderStatus;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderViewDto saveOrder(OrderRequestDto dto);
    OrderViewDto updatedOrder(Long orderId,OrderRequestDto dto);
    OrderViewDto getOrderById(Long orderId);
    public Page<OrderViewDto> getAll(String productName,
                                     OrderStatus status,
                                     int page,
                                     int size,
                                     String sortBy,
                                     String sortDir);

    }
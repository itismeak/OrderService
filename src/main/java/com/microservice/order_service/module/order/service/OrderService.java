package com.microservice.order_service.module.order.service;

import com.microservice.order_service.common.dto.OrderRequestDto;
import com.microservice.order_service.common.dto.OrderUpdateRequestDto;
import com.microservice.order_service.common.dto.OrderViewDto;
import com.microservice.order_service.common.enums.OrderStatus;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderViewDto saveOrder(OrderRequestDto dto) throws BadRequestException;
    OrderViewDto updatedOrder(Long orderId, OrderUpdateRequestDto dto);
    OrderViewDto getOrderById(Long orderId);
    Page<OrderViewDto> getAll(String productName,
                                     OrderStatus status,
                                     int page,
                                     int size,
                                     String sortBy,
                                     String sortDir);

    }
package com.microservice.order_service.module.order.serviceImp;

import com.microservice.order_service.common.component.OrderNumberGenerator;
import com.microservice.order_service.common.dto.OrderRequestDto;
import com.microservice.order_service.common.dto.OrderViewDto;
import com.microservice.order_service.common.enums.OrderStatus;
import com.microservice.order_service.module.order.entity.Order;
import com.microservice.order_service.module.order.repository.OrderRepository;
import com.microservice.order_service.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImp implements OrderService {
    private final OrderRepository orderRepository;
    private OrderNumberGenerator orderNumberGenerator;
    @Override
    public OrderViewDto saveOrder(OrderRequestDto dto) {
        log.info("🟢 [ORDER-CREATE] Request received for userId={}", dto.getUserId());
        Order order=new Order();
        return null;
    }

    @Override
    public OrderViewDto updatedOrder(Long orderId, OrderRequestDto dto) {
        return null;
    }

    @Override
    public OrderViewDto getOrderById(Long orderId) {
        return null;
    }

    @Override
    public Page<OrderViewDto> getAll(String productName, OrderStatus status, int page, int size) {
        return null;
    }
}
package com.microservice.order_service.module.order.serviceImp;

import com.microservice.order_service.common.client.ProductClient;
import com.microservice.order_service.common.client.UserClient;
import com.microservice.order_service.common.component.OrderMapper;
import com.microservice.order_service.common.component.OrderNumberGenerator;
import com.microservice.order_service.common.dto.*;
import com.microservice.order_service.common.enums.OrderItemStatus;
import com.microservice.order_service.common.enums.OrderStatus;
import com.microservice.order_service.common.exceptions.ResourceNotFoundException;
import com.microservice.order_service.module.order.entity.OrderItem;
import com.microservice.order_service.module.order.entity.Orders;
import com.microservice.order_service.module.order.repository.OrderRepository;
import com.microservice.order_service.module.order.service.OrderService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImp implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderMapper orderMapper;
    private final UserClient userClient;
    private final ProductClient productClient;

    @Override
    @Transactional
    public OrderViewDto saveOrder(OrderRequestDto dto) {
        log.info("🟢 [ORDER-CREATE] Request received for userId={} | items={}",
                dto.getUserId(), dto.getItems().size());

        //User validations
        UserViewDto user=userClient.getUserById(dto.getUserId());
        if (user == null) {
            log.error("❌ [ORDER-CREATE] User not found with ID={}", dto.getUserId());
            throw new ResourceNotFoundException("User not registered in the app");
        }
        log.info("👤 [ORDER-CREATE] User found: {}", user.getEmail());

        //Products validations and create order items
        List<OrderItem> orderItems=orderItemsCreate(dto.getItems());
        BigDecimal totalOrderAmount= orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        Orders order=new Orders();
        order.setOrderCode(orderNumberGenerator.generateOrderNumber());
        order.setUserId(user.getId());
        order.setStatus(OrderStatus.PLACED);
        order.setTotalAmount(totalOrderAmount);
        order.setItems(orderItems);

        Orders placeOrder=orderRepository.save(order);
        return orderMapper.toOrderViewDto(placeOrder);
    }
    private List<OrderItem> orderItemsCreate(List<OrderItemRequestDto> otRequest){
        List<OrderItem> orderItems=otRequest.stream()
                .map((item)->{
                    ProductViewDto existProduct= productClient.getProduct(item.getProductId());
                    if(existProduct == null){
                        throw new ResourceNotFoundException(
                                String.format("Product %s not found!",item.getProductName()));
                    }
                    OrderItem orderItem=new OrderItem();
                    orderItem.setProductId(existProduct.getId());
                    orderItem.setProductName(existProduct.getName());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setProductPrice(existProduct.getPrice());

                    BigDecimal price=new BigDecimal(existProduct.getPrice().toString());
                    BigDecimal totalPrice=price.multiply(new BigDecimal(item.getQuantity()));
                    orderItem.setLineTotal(totalPrice);

                    orderItem.setStatus(OrderItemStatus.ACTIVE);
                    return orderItem;
                })
                .toList();
        return orderItems;
    }


    @Override
    public OrderViewDto updatedOrder(Long orderId, OrderRequestDto dto) {
        return null;
    }

    @Override
    public OrderViewDto getOrderById(Long orderId) {
        log.info("Fetching order details for orderId={}", orderId);

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found: {}", orderId);
                    return new RuntimeException("Order not found");
                });

        OrderViewDto res = orderMapper.toOrderViewDto(order);
        // Fetch user info from user-service
        log.info("Fetching user data for userId={}", order.getUserId());
        UserViewDto user = userClient.getUserById(order.getUserId());
        res.setUser(user);
        return res;
    }

    @Override
    public Page<OrderViewDto> getAll(String productName, OrderStatus status, int page, int size, String sortBy, String sortDir) {

        log.info("🟢 [ORDER-GET-ALL] productName={}, status={}, page={}, size={}, sortBy={}, sortDir={}",
                productName, status, page, size, sortBy, sortDir);

        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // If sortBy not in entity fields → fallback to default createdAt
        if (!isValidSortField(sortBy)) {
            log.warn("⚠️ Invalid sortBy='{}'. Falling back to createdAt", sortBy);
            sortBy = "createdAt";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Orders> orders = getOrders(productName, status, pageable);

        return orders.map(order -> {
            OrderViewDto dto = orderMapper.toOrderViewDto(order);
            dto.setUser(fetchUserSafely(order.getUserId()));
            return dto;
        });
    }
    private boolean isValidSortField(String field) {
        return Arrays.stream(Orders.class.getDeclaredFields())
                .anyMatch(f -> f.getName().equals(field));
    }

    private Page<Orders> getOrders(String productName, OrderStatus status, Pageable pageable) {

        boolean hasProduct = productName != null && !productName.isBlank();
        boolean hasStatus = status != null;

        if (hasProduct && hasStatus) {
            return orderRepository.searchByProductNameAndStatus(productName, status, pageable);
        }

        if (hasProduct) {
            return orderRepository.searchByProductName(productName, pageable);
        }

        if (hasStatus) {
            return orderRepository.findByStatus(status, pageable);
        }

        return orderRepository.findAll(pageable);
    }
    private UserViewDto fetchUserSafely(Long userId) {
        try {
            log.info("🟢 [ORDER→USER-SERVICE] Fetching userId={}", userId);
            return userClient.getUserById(userId);

        } catch (FeignException.NotFound ex) {
            log.warn("⚠️ [USER-NOT-FOUND] userId={} does not exist", userId);
            return null; // user deleted or invalid ID

        } catch (FeignException.ServiceUnavailable | FeignException.InternalServerError ex) {
            log.error("❌ [USER-SERVICE-DOWN] User service unavailable for userId={}", userId);
            return null;

        } catch (FeignException ex) {
            log.error("❌ [FEIGN-ERROR] Failed to fetch userId={}, msg={}", userId, ex.getMessage());
            return null;

        } catch (Exception ex) {
            log.error("❌ [UNKNOWN-ERROR] Unexpected error while fetching userId={}", userId, ex);
            return null;
        }
    }

}
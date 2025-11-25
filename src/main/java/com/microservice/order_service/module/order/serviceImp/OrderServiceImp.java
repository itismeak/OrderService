package com.microservice.order_service.module.order.serviceImp;

import com.microservice.order_service.common.client.ProductClient;
import com.microservice.order_service.common.client.UserClient;
import com.microservice.order_service.common.component.OrderMapper;
import com.microservice.order_service.common.component.OrderNumberGenerator;
import com.microservice.order_service.common.dto.*;
import com.microservice.order_service.common.enums.OrderItemStatus;
import com.microservice.order_service.common.enums.OrderStatus;
import com.microservice.order_service.common.enums.ProductStatus;
import com.microservice.order_service.common.enums.UserStatus;
import com.microservice.order_service.common.exceptions.BadRequestException;
import com.microservice.order_service.common.exceptions.ResourceNotFoundException;
import com.microservice.order_service.module.order.entity.OrderItem;
import com.microservice.order_service.module.order.entity.Orders;
import com.microservice.order_service.module.order.repository.OrderItemRepository;
import com.microservice.order_service.module.order.repository.OrderRepository;
import com.microservice.order_service.module.order.service.ExternalClientService;
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
    private final OrderItemRepository orderItemRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderMapper orderMapper;
    private final ExternalClientService externalClientService;


    @Override
    @Transactional
    public OrderViewDto saveOrder(OrderRequestDto dto) {
        log.info("🟢 Request received for userId={} | items={}",
                dto.getUserId(), dto.getItems().size());

        //User validations
        ApiResponse<UserViewDto> user=externalClientService.getUserSafe(dto.getUserId());
        if (user.getData() == null && !user.isStatus()) {
            log.error("❌ User not found with ID={}", dto.getUserId());
            throw new ResourceNotFoundException("User service unavailable or user not found");
        }

        if(user.getData().getStatus() != UserStatus.Active){
            log.error("User {} account was {} ",user.getData().getName(),user.getData().getStatus());
            throw new BadRequestException("User account is not active");
        }

        log.info("👤 User found: {}", user.getData().getEmail());

        //Products validations and create order items
        List<OrderItem> orderItems=orderItemsCreate(dto);
        BigDecimal totalOrderAmount= orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        Orders order=new Orders();
        order.setOrderCode(orderNumberGenerator.generateOrderNumber());
        order.setUserId(user.getData().getId());
        order.setStatus(OrderStatus.Placed);
        order.setTotalAmount(totalOrderAmount);
        //Set fk
        orderItems.forEach(item -> item .setOrder(order));
        order.setItems(orderItems);

        Orders placeOrder=orderRepository.save(order);
        return orderMapper.toOrderViewDto(placeOrder);
    }
    private List<OrderItem> orderItemsCreate(OrderRequestDto request) {

        return request.getItems().stream()
                .map(item -> {

                    ApiResponse<ProductViewDto> response = externalClientService.getProductSafe(item.getProductId());
                    ProductViewDto product = response.getData();

                    if (product == null || !response.isStatus()) {
                        throw new ResourceNotFoundException(
                                "Product not found or product-service unavailable"
                        );
                    }

                    // PRODUCT VALIDATIONS
                    validateProduct(product, item.getQuantity());

                    // Create Order Item
                    BigDecimal price = product.getPrice();
                    BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(product.getId());
                    orderItem.setProductName(product.getName());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setProductPrice(price);
                    orderItem.setLineTotal(lineTotal);
                    orderItem.setStatus(OrderItemStatus.Active);

                    return orderItem;
                })
                .toList();
    }

    private void validateProduct(ProductViewDto product, int reqItemQuantity) {

        if (product.getStatus() != ProductStatus.Available) {
            throw new BadRequestException(
                    "Product '"+product.getName() +"' is not available"
            );
        }

        if (product.getQuantity() < reqItemQuantity) {
            throw new BadRequestException(
                    "Product '" + product.getName() +
                            "' only has " + product.getQuantity() + " quantity in stock"
            );
        }
    }

    @Override
    @Transactional
    public OrderViewDto updatedOrder(Long orderId, OrderUpdateRequestDto dto) {
        log.info("Updated order details for orderId={} ",orderId);
        Orders existOrder=orderRepository.findById(orderId)
                .orElseThrow(()->{
                    log.error("orderId={} not found",orderId);
                    return new ResourceNotFoundException("Order details not found");
                });
        UserViewDto userViewDto=externalClientService.getUserSafe(dto.getUserId()).getData();
        if(userViewDto == null){
            log.error("User id {} not found",dto.getUserId());
            throw new  ResourceNotFoundException("User not registered in the app or User service down");
        }
        if(userViewDto.getStatus() != UserStatus.Active){
            log.error("Order details updated request given user {} status was {}",userViewDto.getName(),
                    userViewDto.getStatus());
            throw new ResourceNotFoundException("User account is not active");
        }
        List<OrderItem> updatedOrderItems=updateOrderItems(dto.getItems());
        boolean allCancelled = updatedOrderItems.stream()
                .allMatch(item -> item.getStatus() == OrderItemStatus.Cancelled);
        BigDecimal totalOrderAmount= updatedOrderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        Orders order=new Orders();
        order.setStatus(allCancelled?OrderStatus.Cancelled:OrderStatus.Placed);
        order.setTotalAmount(totalOrderAmount);
        //Set fk
        updatedOrderItems.forEach(item -> item .setOrder(order));
        order.setItems(updatedOrderItems);

        Orders placeOrder=orderRepository.save(order);
        return orderMapper.toOrderViewDto(placeOrder);
    }
    private List<OrderItem> updateOrderItems(List<OrderItemUpdateRequestDto> request){
        if(request.isEmpty()){
            throw new BadRequestException("Product items nothing selected");
        }
        return request.stream()
                .map(item->{
                    OrderItem orderItem=orderItemRepository.findById(item.getId())
                            .orElseThrow(()->{
                                log.error("Order item for update {} - {}  not found ",
                                        item.getId(),item.getProductName());
                                return  new ResourceNotFoundException("Order Items "+item.getProductName()+" not found");
                            });
                    ProductViewDto product=externalClientService.getProductSafe(item.getProductId()).getData();

                    if (product == null) {
                        throw new ResourceNotFoundException(
                                "Product not found or product-service unavailable"
                        );
                    }
                    validateProduct(product,item.getQuantity());
                    BigDecimal price = product.getPrice();
                    BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setProductPrice(price);
                    orderItem.setLineTotal(lineTotal);
                    orderItem.setStatus(item.getStatus());
                    return orderItem;
                })
                .toList();
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
        ApiResponse<UserViewDto> user = externalClientService.getUserSafe(order.getUserId());
        res.setUser(user.getData());
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
            return externalClientService.getUserSafe(userId).getData();

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
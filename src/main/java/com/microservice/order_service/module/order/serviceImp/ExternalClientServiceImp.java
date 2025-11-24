package com.microservice.order_service.module.order.serviceImp;

import com.microservice.order_service.common.client.ProductClient;
import com.microservice.order_service.common.client.UserClient;
import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.ProductViewDto;
import com.microservice.order_service.common.dto.UserViewDto;
import com.microservice.order_service.module.order.service.ExternalClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalClientServiceImp implements ExternalClientService {
    private final UserClient userClient;
    private final ProductClient productClient;

    // User service
    @Override
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "userFallback")
    public ApiResponse<UserViewDto> getUserSafe(Long userId) {
        log.info("🔵 Calling User-Service for userId={}", userId);
        return userClient.getUserById(userId);
    }

    public ApiResponse<UserViewDto> userFallback(Long userId, Throwable ex) {
        log.error("❌ User-Service unavailable | userId={} | reason={}", userId, ex.getMessage());
        return ApiResponse.error("User service temporarily unavailable. Try again later.");
    }


    // Product service
    @Override
    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "productFallback")
    public ApiResponse<ProductViewDto> getProductSafe(Long productId) {
        log.info("🟣 Calling Product-Service for productId={}", productId);
        return productClient.getProduct(productId);
    }

    public ApiResponse<ProductViewDto> productFallback(Long productId, Throwable ex) {
        log.error("❌ Product-Service unavailable | productId={} | reason={}", productId, ex.getMessage());
        return ApiResponse.error("Product service temporarily unavailable. Try again later.");
    }
}
package com.microservice.order_service.common.component;

import com.microservice.order_service.common.client.ProductClient;
import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.ProductViewDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {
    @Override
    public ApiResponse<ProductViewDto> getProduct(Long id) {
        return ApiResponse.error("Product Service is down. Try later.");
    }

    @Override
    public ApiResponse<Page<ProductViewDto>> getAllProduct(Long id) {
        return ApiResponse.error("Product Service is unavailable.");
    }
}
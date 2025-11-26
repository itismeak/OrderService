package com.microservice.order_service.common.client;

import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.ProductViewDto;
import com.microservice.order_service.common.dto.UserViewDto;

public interface ExternalClientService {
    ApiResponse<UserViewDto> getUserSafe(Long userId);
    ApiResponse<ProductViewDto> getProductSafe(Long productId);
}
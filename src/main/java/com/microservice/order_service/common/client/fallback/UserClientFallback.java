package com.microservice.order_service.common.client.fallback;

import com.microservice.order_service.common.client.UserClient;
import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.UserViewDto;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public ApiResponse<UserViewDto> getUserById(Long id) {
        return ApiResponse.error("User service is down. try again after some time");
    }
}
package com.microservice.order_service.common.client;

import com.microservice.order_service.common.config.FeignClientConfig;
import com.microservice.order_service.common.constants.AppConstant;
import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.UserViewDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Name: microservice name from discovery server (Eureka or Consul)
// URL: only needed if not using service registry
@FeignClient(name = "user-service", url = "http://localhost:8082",
configuration = FeignClientConfig.class)
public interface UserClient {
    @GetMapping(AppConstant.apiVersion+"/user/getUser/{id}")
    ApiResponse<UserViewDto> getUserById(@PathVariable("id") Long id);
}
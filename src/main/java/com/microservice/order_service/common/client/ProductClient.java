package com.microservice.order_service.common.client;

import com.microservice.order_service.common.config.FeignClientConfig;
import com.microservice.order_service.common.constants.AppConstant;
import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.ProductViewDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="product-service",
        configuration = FeignClientConfig.class)
public interface ProductClient {

    @GetMapping(AppConstant.apiVersion+"/product/GetById/{id}")
    ApiResponse<ProductViewDto> getProduct(@PathVariable("id") Long id);
    @GetMapping(AppConstant.apiVersion+"/product/getAll")
    ApiResponse<Page<ProductViewDto>> getAllProduct(@PathVariable("id") Long id);
}
package com.microservice.order_service.common.client;

import com.microservice.order_service.common.constants.AppConstant;
import com.microservice.order_service.common.dto.ProductViewDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="product-service",url = "http://localhost:8083")
public interface ProductClient {

    @GetMapping(AppConstant.apiVersion+"/product/GetById/{id}")
    ProductViewDto getProduct(@PathVariable("id") Long id);
}
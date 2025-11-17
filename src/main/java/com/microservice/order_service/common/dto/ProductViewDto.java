package com.microservice.order_service.common.dto;

import com.microservice.order_service.common.enums.ProductCategory;
import com.microservice.order_service.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductViewDto {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private ProductStatus status; // optional enum
    private ProductCategory category;
}
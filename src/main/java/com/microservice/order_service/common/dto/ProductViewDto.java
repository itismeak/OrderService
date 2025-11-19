package com.microservice.order_service.common.dto;

import com.microservice.order_service.common.enums.ProductCategory;
import com.microservice.order_service.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductViewDto {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private ProductStatus status;
    private ProductCategory category;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
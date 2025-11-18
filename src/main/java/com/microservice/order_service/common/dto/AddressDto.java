package com.microservice.order_service.common.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class AddressDto {
    private Long id;
    private String address1;
    private String address2;
    private String address3;
    private String country;

    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
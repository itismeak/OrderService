package com.microservice.order_service.common.dto;

import com.microservice.order_service.common.enums.Role;
import com.microservice.order_service.common.enums.UserStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class UserViewDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private List<AddressDto> address;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
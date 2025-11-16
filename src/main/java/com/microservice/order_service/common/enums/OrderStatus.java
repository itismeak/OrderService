package com.microservice.order_service.common.enums;

public enum OrderStatus {
    PENDING,        // Order created but not processed
    PLACED,         // Successfully placed (your default)
    CONFIRMED,      // Inventory verified
    PROCESSING,     // Being prepared
    SHIPPED,        // Out for delivery
    DELIVERED,      // Successfully delivered
    CANCELLED,      // Cancelled by user
    FAILED          // Payment / inventory / system failure
}
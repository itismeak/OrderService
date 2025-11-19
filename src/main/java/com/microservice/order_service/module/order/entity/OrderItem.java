package com.microservice.order_service.module.order.entity;

import com.microservice.order_service.common.entity.BaseEntity;
import com.microservice.order_service.common.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private BigDecimal productPrice;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false)
    private BigDecimal lineTotal;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderItemStatus status = OrderItemStatus.Active;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)   // <-- LAZY LOADING
    @JoinColumn(name = "order_id")
    private Orders order;
}
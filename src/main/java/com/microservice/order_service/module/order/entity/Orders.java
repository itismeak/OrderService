package com.microservice.order_service.module.order.entity;

import com.microservice.order_service.common.entity.BaseEntity;
import com.microservice.order_service.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Orders extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false,unique = true)
  private String orderCode;
  @Column(nullable = false)
  private Long userId;
  private BigDecimal totalAmount;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OrderStatus status = OrderStatus.Pending; // default PENDING;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,  // <-- LAZY LOADING
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();
}
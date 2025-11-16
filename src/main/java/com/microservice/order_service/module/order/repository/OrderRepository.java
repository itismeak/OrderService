package com.microservice.order_service.module.order.repository;

import com.microservice.order_service.common.enums.OrderStatus;
import com.microservice.order_service.module.order.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    @Query("""
        SELECT DISTINCT o FROM Orders o
        JOIN o.items i
        WHERE LOWER(i.productName) LIKE LOWER(CONCAT('%', :productName, '%'))
    """)
    Page<Orders> searchByProductName(
            @Param("productName") String productName,
            Pageable pageable
    );

    Page<Orders> findByStatus(OrderStatus status, Pageable pageable);

    @Query("""
        SELECT DISTINCT o FROM Orders o
        JOIN o.items i
        WHERE LOWER(i.productName) LIKE LOWER(CONCAT('%', :productName, '%'))
        AND o.status = :status
    """)
    Page<Orders> searchByProductNameAndStatus(
            @Param("productName") String productName,
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
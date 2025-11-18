package com.microservice.order_service.module.order.controller;

import com.microservice.order_service.common.constants.AppConstant;
import com.microservice.order_service.common.dto.ApiResponse;
import com.microservice.order_service.common.dto.OrderItemRequestDto;
import com.microservice.order_service.common.dto.OrderRequestDto;
import com.microservice.order_service.common.dto.OrderViewDto;
import com.microservice.order_service.common.enums.OrderStatus;
import com.microservice.order_service.module.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(AppConstant.apiVersion+"/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/placeOrder")
    public ResponseEntity<ApiResponse<OrderViewDto>> placeOrder(@RequestBody @Valid OrderRequestDto requestDto){
        log.info("Api Call: Order place request received : {}",requestDto.toString());
        OrderViewDto result=orderService.saveOrder(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                "Order placed successfully",
                result,
                true
                ));
    }

    @GetMapping("/getOrder/{id}")
    public ResponseEntity<ApiResponse<OrderViewDto>> getOrder(@PathVariable Long id){
        log.info("Api Call:  Order retrieval request given id: {}",id);
        OrderViewDto result=orderService.getOrderById(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        "Order details fetched successfully.",
                        result,
                        true
                )
        );
    }

    @GetMapping("/GetAll")
    public ResponseEntity<ApiResponse<Page<OrderViewDto>>> getAllOrders(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {

        log.info("Api call: Get all order details: productName={}, status={}, page={}, size={}, sortBy={}, sortDir={}",
                productName, status, page, size, sortBy, sortDir);

        Page<OrderViewDto> result = orderService.getAll(productName, status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                new ApiResponse<>("Fetched all order data successfully", result, true)
        );
    }



}
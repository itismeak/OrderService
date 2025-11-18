package com.microservice.order_service.common.component;

import com.microservice.order_service.common.dto.*;
import com.microservice.order_service.module.order.entity.OrderItem;
import com.microservice.order_service.module.order.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderMapper {

    private final ModelMapper modelMapper;

    public OrderMapper(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }

    // Convert DTO → OrderItem (NO order yet)
    public OrderItem toOrderItem(OrderItemRequestDto dto){
        log.debug("Mapping OrderItemRequestDto to OrderItem: {}", dto);

        OrderItem item = modelMapper.map(dto, OrderItem.class);

        log.debug("Mapped OrderItem entity: productId={}, qty={}",
                item.getProductId(), item.getQuantity());

        return item;
    }

    // Convert DTO → Order with OrderItems
    public Orders toOrder(OrderRequestDto dto){
        log.info("Mapping OrderRequestDto to Order. userId={}, items={}",
                dto.getUserId(), dto.getItems().size());

        Orders order = new Orders();
        modelMapper.map(dto, order);

        List<OrderItem> items = dto.getItems().stream()
                .map(this::toOrderItem)
                .toList();

        // 👉 Set parent reference
        items.forEach(item -> item.setOrder(order));

        order.setItems(items);

        log.info("Mapped Order entity: userId={}, totalItems={}",
                order.getUserId(), order.getItems().size());

        return order;
    }

    // Convert OrderItem → DTO
    public OrderItemViewDto toOrderItemViewDto(OrderItem entity){
        log.debug("Converting OrderItem entity to DTO: id={}, productId={}",
                entity.getId(), entity.getProductId());

        return modelMapper.map(entity, OrderItemViewDto.class);
    }

    // Convert Order → DTO (handles LAZY loading)
    public OrderViewDto toOrderViewDto(Orders order){
        log.info("Mapping Order entity to OrderViewDto. orderId={}, orderCode={}",
                order.getId(), order.getOrderCode());

        OrderViewDto dto = new OrderViewDto();
        modelMapper.map(order, dto);

        List<OrderItemViewDto> itemsDto = order.getItems().stream()
                .map(this::toOrderItemViewDto)
                .toList();

        dto.setItems(itemsDto);

        log.info("Mapped OrderViewDto: orderId={}, items={}",
                dto.getId(), dto.getItems().size());

        return dto;
    }

//    public OrderItem toOrderItem(ProductViewDto productViewDto,OrderItemRequestDto requestDto){
//        OrderItem orderItem=new OrderItem();
//        orderItem.setProductId(productViewDto.getId());
//        orderItem.setProductName(productViewDto.getName());
//        orderItem.setProductPrice(productViewDto.getPrice());
//        orderItem.setQuantity(requestDto.getQuantity());
//        return orderItem;
//    }
}
package com.microservice.order_service.common.client.producer;

import com.microservice.order_service.common.dto.OrderSaveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderEvent(OrderSaveEvent event) {
        kafkaTemplate.send("order-save-events", event);  // Sends the event to Kafka topic
    }
}
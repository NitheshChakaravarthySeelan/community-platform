package com.community.orders.orderread.kafka;

import com.community.orders.orderread.application.service.OrderService;
import com.community.platform.shared.proto.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "checkout.checkout-events", groupId = "order-read-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Consumed OrderCreatedEvent for order: {}", event.getOrderId());
        try {
            orderService.createOrderFromEvent(event);
        } catch (Exception e) {
            log.error("Failed to sync order for read model: {}", e.getMessage());
        }
    }
}

package com.community.orders.ordercreate.kafka;

import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.application.service.OrderService;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.order.CreateOrderCommand;
import com.community.platform.shared.proto.order.OrderCreatedEvent;
import com.community.platform.shared.proto.order.OrderCreationFailedEvent;
import com.google.protobuf.Timestamp;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderService orderService;

    @Value("${topic.checkout.checkout-events}")
    private String checkoutEventsTopic;

    @KafkaListener(topics = "checkout.order-command", groupId = "order-create-group")
    public void consume(CreateOrderCommand command) {
        log.info("Consumed CreateOrderCommand for saga: {}", command.getMetadata().getSagaId());

        try {
            OrderDTO createdOrder = orderService.createOrder(command);

            OrderCreatedEvent event =
                    OrderCreatedEvent.newBuilder()
                            .setMetadata(
                                    SagaMetadata.newBuilder()
                                            .setSagaId(command.getMetadata().getSagaId())
                                            .setEventId(java.util.UUID.randomUUID().toString())
                                            .setTimestamp(
                                                    Timestamp.newBuilder()
                                                            .setSeconds(
                                                                    Instant.now().getEpochSecond())
                                                            .build())
                                            .build())
                            .setOrderId(createdOrder.getId().toString())
                            .setUserId(createdOrder.getUserId().toString())
                            .setTotalCents(createdOrder.getTotalCents())
                            .build();

            kafkaTemplate.send(checkoutEventsTopic, event);
            log.info("Produced OrderCreatedEvent for order: {}", createdOrder.getId());

        } catch (Exception e) {
            log.error(
                    "Failed to create order for saga {}: {}",
                    command.getMetadata().getSagaId(),
                    e.getMessage());

            OrderCreationFailedEvent failedEvent =
                    OrderCreationFailedEvent.newBuilder()
                            .setMetadata(
                                    SagaMetadata.newBuilder()
                                            .setSagaId(command.getMetadata().getSagaId())
                                            .setEventId(java.util.UUID.randomUUID().toString())
                                            .setTimestamp(
                                                    Timestamp.newBuilder()
                                                            .setSeconds(
                                                                    Instant.now().getEpochSecond())
                                                            .build())
                                            .build())
                            .setUserId(command.getUserId())
                            .setReason(e.getMessage())
                            .build();

            kafkaTemplate.send(checkoutEventsTopic, failedEvent);
        }
    }
}

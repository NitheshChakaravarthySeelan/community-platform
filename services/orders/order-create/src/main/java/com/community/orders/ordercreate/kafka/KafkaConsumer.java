package com.community.orders.ordercreate.kafka;

import com.community.orders.ordercreate.application.command.CreateOrderCommand;
import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.application.service.OrderService;
import com.community.platform.shared.kafka.dto.OrderCreatedEvent;
import com.community.platform.shared.kafka.dto.PaymentProcessedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderService orderService;

    @Value("${topic.checkout.checkout-events}")
    private String checkoutEventsTopic;

    public KafkaConsumer(KafkaTemplate<String, Object> kafkaTemplate, OrderService orderService) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${topic.checkout.checkout-events}", groupId = "order-create-group")
    public void consume(PaymentProcessedEvent event) {
        System.out.println("Consumed PaymentProcessedEvent for order: " + event.getOrderId());

        // Check if the event is a PaymentProcessedEvent
        // In a multi-handler scenario, this would be more sophisticated (e.g., @KafkaHandler)
        // but for now, we assume this listener only cares about PaymentProcessedEvent
        // after refactoring its topic.

        // 1. Map PaymentProcessedEvent to CreateOrderCommand
        CreateOrderCommand createOrderCommand = mapToCreateOrderCommand(event);

        try {
            // 2. Delegate order creation to the OrderService
            OrderDTO createdOrder = orderService.createOrder(createOrderCommand);

            // 3. Produce OrderCreatedEvent
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
            orderCreatedEvent.setOrderId(createdOrder.getId());
            orderCreatedEvent.setUserId(
                    createdOrder.getUserId()); // Assuming UserId is UUID in OrderDTO
            orderCreatedEvent.setTotalAmount(
                    createdOrder.getTotalCents() != null
                            ? new BigDecimal(createdOrder.getTotalCents())
                                    .divide(new BigDecimal(100))
                            : BigDecimal.ZERO); // Convert cents to BigDecimal
            orderCreatedEvent.setTimestamp(Instant.now());

            kafkaTemplate.send(checkoutEventsTopic, orderCreatedEvent);
            System.out.println("Produced OrderCreatedEvent for order: " + createdOrder.getId());

        } catch (Exception e) {
            // TODO: Implement OrderCreationFailedEvent and send it
            System.err.println(
                    "Failed to create order for payment "
                            + event.getPaymentId()
                            + ": "
                            + e.getMessage());
        }
    }

    private CreateOrderCommand mapToCreateOrderCommand(PaymentProcessedEvent event) {
        // This mapping will depend on what PaymentProcessedEvent contains and what
        // CreateOrderCommand expects.
        // For now, we'll map the common fields. Missing fields will require
        // a more complex saga involving earlier steps (e.g., cart details from initial command).
        // This is a simplification to get the happy path working.

        // A real CreateOrderCommand would need more details (items, shipping, billing address,
        // etc.)
        // which would typically come from an earlier event (e.g., CheckoutInitiatedEvent)
        // For now, we'll create a minimal command for the OrderService.

        // Assuming OrderService's CreateOrderCommand can be minimal for this context
        return CreateOrderCommand.builder()
                .userId(event.getUserId())
                .totalCents(
                        event.getAmount() != null
                                ? event.getAmount().multiply(new BigDecimal(100)).intValueExact()
                                : 0) // Convert BigDecimal to cents
                .build();
    }
}

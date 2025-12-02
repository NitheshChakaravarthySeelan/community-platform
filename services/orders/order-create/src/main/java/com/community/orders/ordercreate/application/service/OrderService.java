package com.community.orders.ordercreate.application.service;

import com.community.orders.ordercreate.application.command.CreateOrderCommand;
import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.application.dto.OrderItemDTO;
import com.community.orders.ordercreate.domain.model.Order;
import com.community.orders.ordercreate.domain.model.Status;
import com.community.orders.ordercreate.domain.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List; // ADD THIS IMPORT
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper; // Inject ObjectMapper for JSON processing

    @Transactional
    public OrderDTO createOrder(CreateOrderCommand command) {
        // Generate a new UUID for the order
        UUID orderId = UUID.randomUUID();

        // Map OrderItemCommand list to JSON string
        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(command.getItems());
        } catch (JsonProcessingException e) {
            // In a real application, you'd handle this error more gracefully
            throw new RuntimeException("Failed to serialize order items to JSON", e);
        }

        // Map paymentMethodDetails (assuming it's already a JSON string or simple string)
        String paymentMethodDetailsJson = command.getPaymentMethodDetails();

        Order order =
                Order.builder()
                        .id(orderId)
                        .userId(command.getUserId())
                        .billingAddress(command.getBillingAddress())
                        .shippingAddress(command.getShippingAddress())
                        .items(itemsJson) // Set the JSON string
                        .subtotalCents(command.getSubtotalCents())
                        .shippingCents(command.getShippingCents())
                        .taxCents(command.getTaxCents())
                        .discountCents(command.getDiscountCents())
                        .totalCents(command.getTotalCents())
                        .status(Status.PENDING_PAYMENT) // Initial status
                        .paymentMethodDetails(paymentMethodDetailsJson) // Set the JSON string
                        .transactionId(null) // This would be set after payment processing
                        // createdAt and updatedAt are handled by
                        // @CreationTimestamp/@UpdateTimestamp
                        .build();

        Order savedOrder = orderRepository.save(order);

        // Map the saved Order entity back to OrderDTO
        List<OrderItemDTO> orderItemDTOs = null;
        try {
            orderItemDTOs =
                    objectMapper.readValue(
                            savedOrder.getItems(),
                            objectMapper
                                    .getTypeFactory()
                                    .constructCollectionType(List.class, OrderItemDTO.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize order items from JSON", e);
        }

        return OrderDTO.builder()
                .id(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .billingAddress(savedOrder.getBillingAddress())
                .shippingAddress(savedOrder.getShippingAddress())
                .items(orderItemDTOs)
                .subtotalCents(savedOrder.getSubtotalCents())
                .shippingCents(savedOrder.getShippingCents())
                .taxCents(savedOrder.getTaxCents())
                .discountCents(savedOrder.getDiscountCents())
                .totalCents(savedOrder.getTotalCents())
                .status(savedOrder.getStatus())
                .paymentMethodDetails(savedOrder.getPaymentMethodDetails())
                .transactionId(savedOrder.getTransactionId())
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .build();
    }
}

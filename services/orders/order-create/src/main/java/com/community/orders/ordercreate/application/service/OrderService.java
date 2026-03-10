package com.community.orders.ordercreate.application.service;

import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.application.dto.OrderItemDTO;
import com.community.orders.ordercreate.domain.model.Order;
import com.community.orders.ordercreate.domain.model.Status;
import com.community.orders.ordercreate.domain.repository.OrderRepository;
import com.community.platform.shared.proto.common.Address;
import com.community.platform.shared.proto.order.CreateOrderCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderDTO createOrder(CreateOrderCommand command) {
        String sagaId = command.getMetadata().getSagaId();

        // Idempotency check
        Optional<Order> existingOrder = orderRepository.findBySagaId(sagaId);
        if (existingOrder.isPresent()) {
            return mapToDTO(existingOrder.get());
        }

        UUID orderId = UUID.randomUUID();

        // Map Protobuf OrderItems to JSON string for persistence
        String itemsJson;
        try {
            List<OrderItemDTO> itemDTOs =
                    command.getItemsList().stream()
                            .map(
                                    item ->
                                            OrderItemDTO.builder()
                                                    .productId(UUID.fromString(item.getProductId()))
                                                    .name(item.getName())
                                                    .quantity(item.getQuantity())
                                                    .priceAtTime((int) item.getUnitPriceCents())
                                                    .build())
                            .collect(Collectors.toList());
            itemsJson = objectMapper.writeValueAsString(itemDTOs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize order items to JSON", e);
        }

        Order order =
                Order.builder()
                        .id(orderId)
                        .sagaId(sagaId)
                        .userId(UUID.fromString(command.getUserId()))
                        .billingAddress(formatAddress(command.getBillingAddress()))
                        .shippingAddress(formatAddress(command.getShippingAddress()))
                        .items(itemsJson)
                        .subtotalCents((int) command.getSubtotalCents())
                        .shippingCents((int) command.getShippingCents())
                        .taxCents((int) command.getTaxCents())
                        .discountCents((int) command.getDiscountCents())
                        .totalCents((int) command.getTotalCents())
                        .status(Status.PROCESSING)
                        .transactionId(UUID.fromString(command.getPaymentTransactionId()))
                        .build();

        Order savedOrder = orderRepository.save(order);
        return mapToDTO(savedOrder);
    }

    private String formatAddress(Address address) {
        return String.format(
                "%s, %s, %s, %s %s, %s",
                address.getFullName(),
                address.getStreetAddress(),
                address.getCity(),
                address.getStateProvince(),
                address.getPostalCode(),
                address.getCountry());
    }

    private OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> orderItemDTOs = null;
        try {
            orderItemDTOs =
                    objectMapper.readValue(
                            order.getItems(),
                            objectMapper
                                    .getTypeFactory()
                                    .constructCollectionType(List.class, OrderItemDTO.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize order items from JSON", e);
        }

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .billingAddress(order.getBillingAddress())
                .shippingAddress(order.getShippingAddress())
                .items(orderItemDTOs)
                .subtotalCents(order.getSubtotalCents())
                .shippingCents(order.getShippingCents())
                .taxCents(order.getTaxCents())
                .discountCents(order.getDiscountCents())
                .totalCents(order.getTotalCents())
                .status(order.getStatus())
                .transactionId(order.getTransactionId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

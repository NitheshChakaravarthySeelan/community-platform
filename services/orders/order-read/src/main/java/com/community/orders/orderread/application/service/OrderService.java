package com.community.orders.orderread.application.service;

import com.community.orders.orderread.domain.model.Order;
import com.community.orders.orderread.domain.repository.OrderRepository;
import com.community.orders.orderread.interfaces.dto.OrderItemDTO;
import com.community.orders.orderread.interfaces.dto.OrderResponse;
import com.community.platform.shared.proto.order.OrderCreatedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void createOrderFromEvent(OrderCreatedEvent event) {
        Order order =
                Order.builder()
                        .orderId(UUID.fromString(event.getOrderId()))
                        .sagaId(event.getMetadata().getSagaId())
                        .userId(UUID.fromString(event.getUserId()))
                        .totalAmount(
                                BigDecimal.valueOf(event.getTotalCents())
                                        .divide(BigDecimal.valueOf(100)))
                        .status("COMPLETED")
                        .orderDate(
                                Instant.ofEpochSecond(
                                        event.getMetadata().getTimestamp().getSeconds()))
                        .items(Collections.emptyList()) // Items could be enriched from a separate
                        // event if needed
                        .build();
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Order not found with ID: " + id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDTO> itemDTOs =
                order.getItems() != null
                        ? order.getItems().stream()
                                .map(
                                        item ->
                                                new OrderItemDTO(
                                                        item.getProductId(),
                                                        item.getProductName(),
                                                        item.getQuantity(),
                                                        item.getPrice()))
                                .collect(Collectors.toList())
                        : Collections.emptyList();

        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                itemDTOs,
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate());
    }
}

package com.community.orders.orderread.application.service;

import com.community.orders.orderread.domain.model.Order;
import com.community.orders.orderread.domain.model.OrderItem;
import com.community.orders.orderread.domain.repository.OrderRepository;
import com.community.orders.orderread.interfaces.dto.OrderItemDTO;
import com.community.orders.orderread.interfaces.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                itemDTOs,
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate()
        );
    }

    private OrderItemDTO mapOrderItemToDTO(OrderItem item) {
        return new OrderItemDTO(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}

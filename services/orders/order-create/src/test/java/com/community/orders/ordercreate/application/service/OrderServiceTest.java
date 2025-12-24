package com.community.orders.ordercreate.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.community.orders.ordercreate.application.command.CreateOrderCommand;
import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.domain.model.Order;
import com.community.orders.ordercreate.domain.model.Status;
import com.community.orders.ordercreate.domain.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private OrderRepository orderRepository;

    @Mock private ObjectMapper objectMapper;

    @InjectMocks private OrderService orderService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock the repository's save method to return the order it was given
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock the ObjectMapper to prevent it from throwing errors on serialization/deserialization
        when(objectMapper.writeValueAsString(any()))
                .thenReturn("[]"); // Return empty JSON array string

        // Make the readValue mock more flexible to handle the CollectionType
        when(objectMapper.readValue(any(String.class), any(CollectionType.class)))
                .thenReturn(Collections.emptyList());

        when(objectMapper.getTypeFactory()).thenReturn(new ObjectMapper().getTypeFactory());
    }

    @Test
    void whenCreateOrder_withValidCommand_thenReturnsOrderDTO() {
        // Arrange
        UUID userId = UUID.randomUUID();
        CreateOrderCommand command =
                CreateOrderCommand.builder()
                        .userId(userId)
                        .billingAddress("123 Main St")
                        .shippingAddress("123 Main St")
                        .items(Collections.emptyList())
                        .totalCents(10000)
                        .build();

        // Act
        OrderDTO result = orderService.createOrder(command);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(Status.PENDING_PAYMENT, result.getStatus());
        assertEquals(10000, result.getTotalCents());
    }
}

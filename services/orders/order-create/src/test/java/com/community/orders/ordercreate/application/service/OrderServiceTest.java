package com.community.orders.ordercreate.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.community.orders.ordercreate.application.command.CreateOrderCommand;
import com.community.orders.ordercreate.application.command.OrderItemCommand;
import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.application.dto.OrderItemDTO;
import com.community.orders.ordercreate.domain.model.Order;
import com.community.orders.ordercreate.domain.model.Status;
import com.community.orders.ordercreate.domain.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;

    @Mock private ObjectMapper objectMapper;

    @Mock private com.fasterxml.jackson.databind.type.TypeFactory typeFactory; // New mock

    @Mock
    private com.fasterxml.jackson.databind.type.CollectionType
            collectionType; // Corrected mock for CollectionType

    @InjectMocks private OrderService orderService;

    private CreateOrderCommand createOrderCommand;

    private OrderItemCommand orderItemCommand;

    @BeforeEach
    void setUp() throws JsonProcessingException {

        orderItemCommand =
                OrderItemCommand.builder()
                        .productId(UUID.randomUUID())
                        .quantity(2)
                        .name("Test Product")
                        .priceAtTime(1000)
                        .build();

        createOrderCommand =
                CreateOrderCommand.builder()
                        .userId(UUID.randomUUID())
                        .billingAddress("123 Test St")
                        .shippingAddress("456 Test Ave")
                        .items(Collections.singletonList(orderItemCommand))
                        .paymentMethodDetails("Card **** 1111")
                        .subtotalCents(2000)
                        .shippingCents(100)
                        .taxCents(200)
                        .discountCents(0)
                        .totalCents(2300)
                        .build();

        // Mock ObjectMapper behavior

        when(objectMapper.getTypeFactory()).thenReturn(typeFactory); // Return mock TypeFactory

        when(typeFactory.constructCollectionType(any(Class.class), any(Class.class)))
                .thenReturn(collectionType); // Return mock CollectionType

        when(objectMapper.writeValueAsString(any(List.class)))
                .thenReturn("[{\"productId\":\"...\"}]");

        // Corrected stubbing for readValue

        when(objectMapper.readValue(
                        anyString(),
                        any(
                                com.fasterxml.jackson.databind.type.CollectionType
                                        .class))) // Use any(CollectionType.class)
                .thenReturn(
                        Collections.singletonList(
                                OrderItemDTO.builder()
                                        .productId(orderItemCommand.getProductId())
                                        .quantity(orderItemCommand.getQuantity())
                                        .name(orderItemCommand.getName())
                                        .priceAtTime(orderItemCommand.getPriceAtTime())
                                        .build()));
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        Order savedOrder =
                Order.builder()
                        .id(UUID.randomUUID())
                        .userId(createOrderCommand.getUserId())
                        .status(Status.PENDING_PAYMENT)
                        .totalCents(createOrderCommand.getTotalCents())
                        .items("[{\"productId\":\"...\"}]")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        OrderDTO result = orderService.createOrder(createOrderCommand);

        // Then
        assertNotNull(result);
        assertThat(result.getStatus()).isEqualTo(Status.PENDING_PAYMENT);
        assertThat(result.getUserId()).isEqualTo(createOrderCommand.getUserId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}

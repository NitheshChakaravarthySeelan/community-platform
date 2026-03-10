package com.community.orders.ordercreate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.community.orders.ordercreate.application.dto.OrderDTO;
import com.community.orders.ordercreate.application.service.OrderService;
import com.community.orders.ordercreate.domain.model.Order;
import com.community.orders.ordercreate.domain.model.Status;
import com.community.orders.ordercreate.domain.repository.OrderRepository;
import com.community.platform.shared.proto.common.Address;
import com.community.platform.shared.proto.common.SagaMetadata;
import com.community.platform.shared.proto.order.CreateOrderCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OrderServiceTest {

    private OrderService orderService;

    @Mock private OrderRepository repository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        orderService = new OrderService(repository, objectMapper);
    }

    @Test
    void testCreateOrder_Success() {
        String sagaId = "order-saga-123";
        CreateOrderCommand command =
                CreateOrderCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .setUserId(UUID.randomUUID().toString())
                        .setPaymentTransactionId(UUID.randomUUID().toString())
                        .setTotalCents(5000)
                        .setBillingAddress(
                                Address.newBuilder()
                                        .setFullName("John Doe")
                                        .setStreetAddress("123 Main St")
                                        .setCity("New York")
                                        .setStateProvince("NY")
                                        .setPostalCode("10001")
                                        .setCountry("USA")
                                        .build())
                        .setShippingAddress(Address.newBuilder().setFullName("John Doe").build())
                        .build();

        when(repository.findBySagaId(sagaId)).thenReturn(Optional.empty());
        when(repository.save(any(Order.class)))
                .thenAnswer(
                        invocation -> {
                            Order order = invocation.getArgument(0);
                            return order;
                        });

        OrderDTO result = orderService.createOrder(command);

        assertNotNull(result);
        assertEquals(Status.PROCESSING, result.getStatus());
        verify(repository, times(1)).save(any());
    }

    @Test
    void testCreateOrder_Idempotency() {
        String sagaId = "existing-order-saga";
        CreateOrderCommand command =
                CreateOrderCommand.newBuilder()
                        .setMetadata(SagaMetadata.newBuilder().setSagaId(sagaId).build())
                        .build();

        Order existing =
                Order.builder()
                        .id(UUID.randomUUID())
                        .sagaId(sagaId)
                        .status(Status.PROCESSING)
                        .items("[]")
                        .build();

        when(repository.findBySagaId(sagaId)).thenReturn(Optional.of(existing));

        OrderDTO result = orderService.createOrder(command);

        assertEquals(existing.getId(), result.getId());
        verify(repository, never()).save(any());
    }
}

package com.community.orders.ordercreate.interfaces.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.community.orders.ordercreate.OrderCreateApplication;
import com.community.orders.ordercreate.application.command.CreateOrderCommand;
import com.community.orders.ordercreate.application.command.OrderItemCommand;
import com.community.orders.ordercreate.domain.model.Status;
import com.community.orders.ordercreate.domain.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// Removed @Testcontainers
@SpringBootTest(classes = OrderCreateApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test-h2") // Activate a specific profile for H2 database
class OrderControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private OrderRepository orderRepository;

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
  }

  @Test
  void shouldCreateOrderSuccessfully() throws Exception {
    // Given
    OrderItemCommand orderItemCommand = OrderItemCommand.builder()
        .productId(UUID.randomUUID())
        .quantity(1)
        .name("Integration Test Product")
        .priceAtTime(1500)
        .build();

    CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
        .userId(UUID.randomUUID())
        .billingAddress("456 Integration St")
        .shippingAddress("789 Integration Ave")
        .items(Collections.singletonList(orderItemCommand))
        .paymentMethodDetails("Integration Card **** 2222")
        .subtotalCents(1500)
        .shippingCents(50)
        .taxCents(150)
        .discountCents(0)
        .totalCents(1700)
        .build();

    // When
    mockMvc.perform(
        post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createOrderCommand)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.userId").value(createOrderCommand.getUserId().toString()))
        .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));

    // Then verify that the order was actually saved to the database
    assertThat(orderRepository.count()).isEqualTo(1);
    orderRepository
        .findAll()
        .forEach(
            order -> {
              assertThat(order.getUserId()).isEqualTo(createOrderCommand.getUserId());
              assertThat(order.getStatus()).isEqualTo(Status.PENDING_PAYMENT);
            });
  }
}

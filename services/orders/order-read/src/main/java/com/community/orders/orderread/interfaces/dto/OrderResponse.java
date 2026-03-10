package com.community.orders.orderread.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID orderId;
    private UUID userId;
    private List<OrderItemDTO> items; // Assuming OrderItemDTO will be defined
    private BigDecimal totalAmount;
    private String status; // e.g., "PENDING", "COMPLETED", "CANCELLED"
    private Instant orderDate;
}

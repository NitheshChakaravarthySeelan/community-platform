package com.community.orders.ordercreate.application.command;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This represents the incoming request payload from the checkout orchestrator
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {
    private UUID userId;
    private String billingAddress; // Simple string for now, could be a separate DTO
    private String shippingAddress; // Simple string for now, could be a separate DTO
    private List<OrderItemCommand> items; // List of items in the order
    private String paymentMethodDetails; // Simple string for now, could be a separate DTO
    private Integer subtotalCents;
    private Integer shippingCents;
    private Integer taxCents;
    private Integer discountCents;
    private Integer totalCents;
}

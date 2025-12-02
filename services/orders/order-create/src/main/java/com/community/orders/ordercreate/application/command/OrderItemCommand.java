package com.community.orders.ordercreate.application.command;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCommand {
    private UUID productId;
    private Integer quantity;
    private String name; // Snapshot of product name
    private Integer priceAtTime; // Snapshot of price at time of order
}

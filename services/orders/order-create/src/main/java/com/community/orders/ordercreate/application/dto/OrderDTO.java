package com.community.orders.ordercreate.application.dto;

import com.community.orders.ordercreate.domain.model.Status;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This represents the output response sent back to the client
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private UUID id;
    private UUID userId;
    private String billingAddress;
    private String shippingAddress;
    private List<OrderItemDTO> items; // Using a dedicated DTO for order items
    private Integer subtotalCents;
    private Integer shippingCents;
    private Integer taxCents;
    private Integer discountCents;
    private Integer totalCents;
    private Status status;
    private String paymentMethodDetails;
    private UUID transactionId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

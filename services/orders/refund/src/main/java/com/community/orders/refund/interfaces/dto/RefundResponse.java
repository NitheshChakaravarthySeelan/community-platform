package com.community.orders.refund.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private UUID refundId;
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String status; // e.g., "PENDING", "COMPLETED", "FAILED"
    private String reason;
    private Instant refundDate;
    private UUID transactionId; // Link to the payment gateway transaction
}

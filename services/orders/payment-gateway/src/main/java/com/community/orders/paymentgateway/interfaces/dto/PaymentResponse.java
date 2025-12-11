package com.community.orders.paymentgateway.interfaces.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID transactionId;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private String status; // e.g., "SUCCESS", "FAILED", "PENDING"
    private String message; // e.g., "Payment successful", "Insufficient funds"
    private Instant timestamp;
}

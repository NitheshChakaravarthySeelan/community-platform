package com.community.orders.paymentgateway.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private UUID orderId;
    private BigDecimal amount;
    private String currency; // e.g., "USD", "EUR"
    private String paymentMethod; // e.g., "CREDIT_CARD", "PAYPAL"
    private Map<String, Object> paymentMethodDetails; // Card number, expiry, etc. (sensitive data should be handled carefully)
}

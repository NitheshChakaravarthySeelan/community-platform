package com.community.orders.refund.interfaces.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String reason;
}

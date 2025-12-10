package com.community.orders.wallet.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private UUID transactionId;
    private UUID userId;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String transactionType;
    private String status; // e.g., "SUCCESS", "FAILED"
    private String message;
    private Instant timestamp;
    private UUID referenceId;
}

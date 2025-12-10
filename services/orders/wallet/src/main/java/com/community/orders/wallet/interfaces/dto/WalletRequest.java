package com.community.orders.wallet.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {
    private BigDecimal amount;
    private String transactionType; // e.g., "CREDIT", "DEBIT"
    private UUID referenceId; // e.g., orderId, refundId
}

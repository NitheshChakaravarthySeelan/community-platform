package com.community.orders.wallet.interfaces.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {
    private BigDecimal amount;
    private String transactionType; // e.g., "CREDIT", "DEBIT"
    private UUID referenceId; // e.g., orderId, refundId
}

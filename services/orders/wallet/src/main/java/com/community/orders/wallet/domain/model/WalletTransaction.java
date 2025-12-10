package com.community.orders.wallet.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String transactionType; // "CREDIT" or "DEBIT"

    @Column(nullable = false)
    private String status; // "SUCCESS", "FAILED"

    @Column(nullable = true)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(nullable = true)
    private UUID referenceId; // e.g., orderId, refundId

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

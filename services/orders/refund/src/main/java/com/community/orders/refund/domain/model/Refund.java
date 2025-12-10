package com.community.orders.refund.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refunds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID refundId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status; // e.g., "PENDING", "COMPLETED", "FAILED"

    @Column(nullable = true)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant refundDate;

    @Column(nullable = true)
    private UUID transactionId; // Link to the payment gateway transaction

    @PrePersist
    protected void onCreate() {
        if (refundDate == null) {
            refundDate = Instant.now();
        }
    }
}

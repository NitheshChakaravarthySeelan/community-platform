package com.community.pricing.discountengine.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "discount_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "SUMMER20", "FREESHIP"

    @Column(nullable = false)
    private String type; // e.g., "PERCENT_OFF", "AMOUNT_OFF", "BUY_ONE_GET_ONE_FREE"

    @Column(nullable = false)
    private BigDecimal value; // e.g., 0.20 for 20% off, 10.00 for $10 off

    @Column(nullable = true)
    private Integer minItems; // Minimum items in cart to apply discount

    @Column(nullable = true)
    private BigDecimal minPurchase; // Minimum total purchase to apply discount

    @Column(nullable = true)
    private UUID appliesToProductId; // If discount applies to a specific product

    @Column(nullable = true)
    private String appliesToCategoryId; // If discount applies to a specific category

    @Column(nullable = true)
    private Instant expirationDate;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastUpdated;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (lastUpdated == null) {
            lastUpdated = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }
}

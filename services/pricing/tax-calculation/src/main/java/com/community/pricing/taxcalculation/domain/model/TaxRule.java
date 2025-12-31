package com.community.pricing.taxcalculation.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "tax_rules")
@Data
public class TaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String country;

    private String state; // Can be null if it's a country-wide tax

    @Column(nullable = false)
    private BigDecimal taxRatePercentage;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

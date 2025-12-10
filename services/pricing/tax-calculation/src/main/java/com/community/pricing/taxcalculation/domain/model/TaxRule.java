package com.community.pricing.taxcalculation.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tax_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxRule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String state; // e.g., "CA", "NY"

    @Column(nullable = false)
    private String country; // e.g., "US", "CA"

    @Column(nullable = false)
    private BigDecimal taxRate; // e.g., 0.0825 for 8.25%

    @Column(nullable = true)
    private String category; // e.g., "FOOD", "CLOTHING" (for category-specific taxes)
}

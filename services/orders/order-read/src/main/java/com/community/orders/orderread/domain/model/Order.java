package com.community.orders.orderread.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Column(nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Cascade(CascadeType.ALL)
    private List<OrderItem> items;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status; // e.g., "PENDING", "COMPLETED", "CANCELLED"

    @Column(nullable = false, updatable = false)
    private Instant orderDate;

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = Instant.now();
        }
    }
}
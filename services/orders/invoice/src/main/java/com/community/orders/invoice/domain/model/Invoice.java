package com.community.orders.invoice.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID invoiceId;

    @Column(nullable = false, unique = true)
    private UUID orderId; // Link to the order

    @Column(nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    @Cascade(CascadeType.ALL)
    private List<InvoiceItem> items;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, updatable = false)
    private Instant invoiceDate;

    @Column(nullable = false)
    private String paymentStatus; // e.g., "PAID", "PENDING", "OVERDUE"

    @PrePersist
    protected void onCreate() {
        if (invoiceDate == null) {
            invoiceDate = Instant.now();
        }
    }
}

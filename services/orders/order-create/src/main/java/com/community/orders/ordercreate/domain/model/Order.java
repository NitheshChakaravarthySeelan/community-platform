package com.community.orders.ordercreate.domain.model;

import com.vladmihalcea.hibernate.type.json.JsonStringType; // NEW IMPORT
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime; // Use OffsetDateTime for TIMESTAMPTZ
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type; // NEW IMPORT
import org.hibernate.annotations.UpdateTimestamp;

// 1. Add all necessary annotations
@Entity
@Table(name = "orders")
@Getter // Use individual annotations instead of @Data
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    // 2. Remove @GeneratedValue - UUIDs will be assigned in the service layer
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String billingAddress; // Assuming this is a snapshot, String is okay for now

    private String shippingAddress;

    // 3. Keep as String for now, but be aware of more advanced options
    @Type(value = JsonStringType.class) // MODIFIED
    private String items;

    // Corrected field names to match Java conventions
    private Integer subtotalCents;
    private Integer shippingCents;
    private Integer taxCents;
    private Integer discountCents;

    @Column(nullable = false)
    private Integer totalCents;

    // 4. Use EnumType.STRING for robust enum mapping
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "VARCHAR")
    @Type(value = JsonStringType.class) // MODIFIED
    private String paymentMethodDetails;

    private UUID transactionId; // Note: 'transactionId' is a better name than 'transactionID'

    // 5. Use OffsetDateTime and automatic timestamping
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}

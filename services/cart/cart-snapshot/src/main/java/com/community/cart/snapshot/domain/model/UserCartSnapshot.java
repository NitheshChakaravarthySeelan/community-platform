package com.community.cart.snapshot.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cart_snapshots")
@Getter
@Setter
@NoArgsConstructor // Lombok will generate a no-argument constructor
@AllArgsConstructor // Lombok will generate a constructor with all fields
public class UserCartSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    // LLD: Using JSONB to store a flexible, schemaless copy of the cart items array.
    // This is the core of the "snapshot" - it's the exact state of the cart at a moment in time.
    @JdbcTypeCode(SqlTypes.JSON) // Corrected from SqlTypes.json
    @Column(nullable = false, columnDefinition = "jsonb")
    private String items; // Stored as a JSON string

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() { // Marked protected as it's typically internal to JPA
        if (createdAt == null) { // Ensure it's only set once on creation
            createdAt = Instant.now();
        }
    }
}

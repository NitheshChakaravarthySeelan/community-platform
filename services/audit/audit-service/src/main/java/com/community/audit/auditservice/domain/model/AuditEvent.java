package com.community.audit.auditservice.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String eventType; // e.g., "USER_LOGIN", "PRODUCT_UPDATED", "CART_CHECKOUT"

    @Column(nullable = false)
    private String entityType; // e.g., "User", "Product", "Cart"

    @Column(nullable = false)
    private UUID entityId; // ID of the entity that was affected

    @Column(nullable = false)
    private UUID actorId; // ID of the user/service that performed the action

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload; // Detailed information about the event

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}

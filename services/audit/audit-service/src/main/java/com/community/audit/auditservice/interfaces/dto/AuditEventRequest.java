package com.community.audit.auditservice.interfaces.dto;

import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventRequest {
    private String eventType;
    private String entityType;
    private UUID entityId; // ID of the entity that was affected
    private UUID actorId; // ID of the user/service that performed the action
    private Map<String, Object> payload; // Details of the event (e.g., old value, new value)
}

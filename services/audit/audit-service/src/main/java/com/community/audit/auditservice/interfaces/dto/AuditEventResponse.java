package com.community.audit.auditservice.interfaces.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {
    private UUID id;
    private String eventType;
    private String entityType;
    private UUID entityId;
    private UUID actorId;
    private Map<String, Object> payload;
    private Instant timestamp;
}

package com.community.audit.auditservice.application.service;

import com.community.audit.auditservice.domain.model.AuditEvent;
import com.community.audit.auditservice.domain.repository.AuditEventRepository;
import com.community.audit.auditservice.interfaces.dto.AuditEventRequest;
import com.community.audit.auditservice.interfaces.dto.AuditEventResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public AuditEventResponse recordAuditEvent(AuditEventRequest request) {
        AuditEvent auditEvent =
                AuditEvent.builder()
                        .eventType(request.getEventType())
                        .entityType(request.getEntityType())
                        .entityId(request.getEntityId())
                        .actorId(request.getActorId())
                        .payload(request.getPayload())
                        .timestamp(Instant.now()) // Set timestamp at service level
                        .build();

        AuditEvent savedEvent = auditEventRepository.save(auditEvent);
        return mapToResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public AuditEventResponse getAuditEventById(UUID id) {
        AuditEvent auditEvent =
                auditEventRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Audit event not found with ID: " + id));
        return mapToResponse(auditEvent);
    }

    private AuditEventResponse mapToResponse(AuditEvent auditEvent) {
        return new AuditEventResponse(
                auditEvent.getId(),
                auditEvent.getEventType(),
                auditEvent.getEntityType(),
                auditEvent.getEntityId(),
                auditEvent.getActorId(),
                auditEvent.getPayload(),
                auditEvent.getTimestamp());
    }
}

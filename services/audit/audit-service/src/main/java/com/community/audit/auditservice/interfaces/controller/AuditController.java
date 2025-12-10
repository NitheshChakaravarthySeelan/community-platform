package com.community.audit.auditservice.interfaces.controller;

import com.community.audit.auditservice.application.service.AuditService;
import com.community.audit.auditservice.interfaces.dto.AuditEventRequest;
import com.community.audit.auditservice.interfaces.dto.AuditEventResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-events")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<AuditEventResponse> recordAuditEvent(
            @RequestBody AuditEventRequest request) {
        AuditEventResponse response = auditService.recordAuditEvent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditEventResponse> getAuditEventById(@PathVariable UUID id) {
        AuditEventResponse response = auditService.getAuditEventById(id);
        return ResponseEntity.ok(response);
    }
}

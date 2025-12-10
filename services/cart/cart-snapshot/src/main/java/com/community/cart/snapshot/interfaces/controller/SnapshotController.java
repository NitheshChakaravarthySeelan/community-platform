package com.community.cart.snapshot.interfaces.controller;

import com.community.cart.snapshot.application.service.SnapshotService;
import com.community.cart.snapshot.domain.model.UserCartSnapshot;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/snapshots") // Corrected to plural for consistency
public class SnapshotController {
    private final SnapshotService snapshotService;

    // LLD: Dependency Injection
    public SnapshotController(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<UUID> createCartSnapshot(@PathVariable Long userId) {
        // Corrected method call from createCartSnapshot to createSnapshot
        UserCartSnapshot snapshot = snapshotService.createSnapshot(userId);
        // Lombok's @Getter should provide getId(). The previous error might have been due to
        // SnapshotService not compiling correctly due to its own errors.
        return new ResponseEntity<>(snapshot.getId(), HttpStatus.CREATED);
    }

    @GetMapping("/{snapshotId}")
    public ResponseEntity<UserCartSnapshot> getCartSnapshotById(@PathVariable UUID snapshotId) {
        UserCartSnapshot snapshot = snapshotService.getSnapshotById(snapshotId);
        return ResponseEntity.ok(snapshot);
    }
}

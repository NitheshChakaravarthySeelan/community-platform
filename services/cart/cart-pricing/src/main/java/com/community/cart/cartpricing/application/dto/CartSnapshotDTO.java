package com.community.cart.cartpricing.application.dto;

import java.time.Instant;
import java.util.UUID;

// LLD: DTO representing the response structure from the cart-snapshot service
public record CartSnapshotDTO(
    UUID id,
    Long userId,
    String items, // This will be the JSON string of items from the snapshot
    Instant createdAt
) {}

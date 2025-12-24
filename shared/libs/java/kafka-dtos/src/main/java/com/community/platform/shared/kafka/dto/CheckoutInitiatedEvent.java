package com.community.platform.shared.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CheckoutInitiatedEvent {
  private UUID orderId; // This will act as the saga ID
  private UUID userId;
  private List<InventoryItem> items; // Items being purchased
  private BigDecimal totalAmount;
  private Instant timestamp;
}

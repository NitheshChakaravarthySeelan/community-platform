package com.community.platform.shared.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OrderCreatedEvent {
  private UUID orderId;
  private UUID userId; // Added userId
  private BigDecimal totalAmount; // Added totalAmount
  private Instant timestamp;
}

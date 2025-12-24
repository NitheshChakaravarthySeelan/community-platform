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
public class PaymentProcessedEvent {
  private UUID orderId;
  private UUID paymentId;
  private UUID userId; // Added userId
  private BigDecimal amount; // Added amount
  private Instant timestamp;
}

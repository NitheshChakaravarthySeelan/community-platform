package com.community.platform.shared.kafka.dto;

import java.time.Instant;
import java.util.UUID; // Assuming orderId might be UUID
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PaymentFailedEvent {
  private UUID orderId; // Assuming orderId is UUID
  private String reason;
  private Instant timestamp;
}

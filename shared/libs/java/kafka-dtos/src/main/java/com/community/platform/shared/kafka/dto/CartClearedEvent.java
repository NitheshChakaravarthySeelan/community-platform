package com.community.platform.shared.kafka.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CartClearedEvent {
  private UUID orderId;
  private UUID userId;
  private Instant timestamp;
}

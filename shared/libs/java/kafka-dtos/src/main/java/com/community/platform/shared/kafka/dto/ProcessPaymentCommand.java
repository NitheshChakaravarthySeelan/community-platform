package com.community.platform.shared.kafka.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProcessPaymentCommand {
  private UUID orderId;
  private String userId;
  private BigDecimal amount;
}

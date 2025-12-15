package com.community.orders.paymentgateway.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PaymentProcessedEvent {
  private String objectId;
  private String paymentId;
  private Instant timestamp;
}

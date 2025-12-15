package com.community.orders.paymentgateway.application.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class PaymentFailedEvent {
  private String orderId;
  private String reason;
  private Instant timestamp;
}

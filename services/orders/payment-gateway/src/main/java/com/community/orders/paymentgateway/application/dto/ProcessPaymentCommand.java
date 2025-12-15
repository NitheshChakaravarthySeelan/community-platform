package com.community.orders.paymentgateway.application.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProcessPaymentCommand {
  private String orderId;
  private String userId;
  private BigDecimal amount;
}

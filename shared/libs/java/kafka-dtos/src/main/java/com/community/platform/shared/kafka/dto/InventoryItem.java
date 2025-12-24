package com.community.platform.shared.kafka.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class InventoryItem {
  private String productId;
  private int quantity;
}

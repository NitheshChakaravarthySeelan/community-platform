package com.community.platform.shared.kafka.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ReserveInventoryCommand {
  private UUID orderId;
  private UUID userId;
  private List<InventoryItem> items;
}

package com.community.pricing.listprice.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceRequest {
    private UUID productId;
    private BigDecimal price;
    private String currency; // e.g., "USD", "EUR"
}

package com.community.pricing.listprice.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {
    private UUID id;
    private UUID productId;
    private BigDecimal price;
    private String currency;
    private Instant lastUpdated;
}

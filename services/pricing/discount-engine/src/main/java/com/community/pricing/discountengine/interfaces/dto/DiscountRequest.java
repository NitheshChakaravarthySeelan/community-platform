package com.community.pricing.discountengine.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRequest {
    private List<ProductPriceDTO> products;
    private String couponCode;
    private UUID userId; // Optional, for user-specific discounts
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ProductPriceDTO {
    private UUID productId;
    private BigDecimal price;
    private int quantity;
}

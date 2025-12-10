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
public class DiscountResponse {
    private List<DiscountedProductDTO> discountedProducts;
    private BigDecimal totalOriginalAmount;
    private BigDecimal totalDiscountedAmount;
    private BigDecimal totalDiscountAmount;
    private String appliedCouponCode;
    private String message; // e.g., "Discount applied successfully"
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class DiscountedProductDTO {
    private UUID productId;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private BigDecimal discountAmount;
    private String appliedRule; // e.g., "PERCENT_OFF", "BUY_ONE_GET_ONE_FREE"
}

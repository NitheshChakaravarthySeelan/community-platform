package com.community.pricing.discountengine.interfaces.dto;

import java.util.List;
import lombok.Data;

// A simple representation of cart details for the request
@Data
public class DiscountRequest {
    private String cartId;
    private String userId;
    private List<CartItemDTO> items;

    @Data
    public static class CartItemDTO {
        private String productId;
        private int quantity;
        private long priceCents;
    }
}

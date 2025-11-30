package com.community.cart.cartpricing.application.dto;

public record PricedCartItem(
    String productId,
    int quantity,
    String name,
    long unitPriceCents,
    long lineItemTotalCents
) {}

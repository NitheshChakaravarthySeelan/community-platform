package com.community.cart.cartpricing.application.dto;

import java.util.List;
import java.util.UUID;

public record CartPricingResponse(
    UUID cartSnapshotId,
    List<PricedCartItem> items,
    long subtotalCents,
    long shippingCents,
    long taxCents,
    long discountCents,
    long totalCents
) {}

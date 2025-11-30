package com.community.cart.cartpricing.application.service;

import com.community.cart.cartpricing.application.dto.CartPricingRequest;
import com.community.cart.cartpricing.application.dto.CartPricingResponse;
import com.community.cart.cartpricing.application.dto.PricedCartItem;
import com.community.cart.cartpricing.infrastructure.client.CartSnapshotAdapter;
import com.community.cart.cartpricing.application.dto.CartSnapshotDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // Lombok generates constructor for final fields
public class CartPricingService {

    private final CartSnapshotAdapter cartSnapshotAdapter;

    public CartPricingResponse calculatePrice(CartPricingRequest request) {
        // HLD: Pricing operates on an immutable snapshot for integrity.
        // LLD: Dependency Injection to manage adapters.
        CartSnapshotDTO snapshot = cartSnapshotAdapter.getCartSnapshot(request.cartSnapshotId());

        // Parse items from the snapshot
        List<PricedCartItem> snapshotItems = cartSnapshotAdapter.parseSnapshotItems(snapshot.items());

        // LLD: Placeholder for complex pricing logic and future discount engine integration.
        long subtotalCents = 0;
        long shippingCents = 0; // Fixed for now, future service
        long taxCents = 0; // Fixed for now, future service
        long discountCents = 0; // Future: call discount-engine service via gRPC

        for (PricedCartItem item : snapshotItems) {
            // For now, assume prices are accurate in the snapshot items themselves (or product-read integration needed)
            subtotalCents += item.lineItemTotalCents();
        }

        long totalCents = subtotalCents + shippingCents + taxCents - discountCents;

        return new CartPricingResponse(
            snapshot.id(),
            snapshotItems,
            subtotalCents,
            shippingCents,
            taxCents,
            discountCents,
            totalCents
        );
    }
}

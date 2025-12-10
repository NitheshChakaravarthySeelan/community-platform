package com.community.cart.snapshot.infrastructure.client;

import com.community.cart.snapshot.config.CartSnapshotConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CartCrudAdapter {
    private final RestTemplate restTemplate;
    private final String cartCrudUrl;

    // LLD: Inject the entire config object for clean, type-safe access to properties.
    public CartCrudAdapter(RestTemplate restTemplate, CartSnapshotConfig config) {
        this.restTemplate = restTemplate;
        this.cartCrudUrl = config.getClients().getCartCrud().getUrl();
    }

    /** Fetch the live cart data (as a JSON string) from the cart-crud service. */
    public String getLiveCart(Long userId) {
        String url = cartCrudUrl + "/api/v1/carts/" + userId;

        try {
            // Send the request
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // Corrected: Use '+' for string concatenation and IllegalStateException
            System.err.println("Error fetching cart for user " + userId + ": " + e.getMessage());
            throw new IllegalStateException("Could not fetch live cart for user: " + userId, e);
        }
    }
}

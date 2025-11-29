package com.community.cart.snapshot.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// Import for general SQL exceptions if needed, but
// IllegalStateException is fine for now

@Component
public class CartCrudAdapter {
    private final RestTemplate restTemplate;
    private final String cartCrudUrl;

    // HLD: Url for the dependency injected from the application.properties
    // The Value annotation will inject the values from application.properties
    public CartCrudAdapter(
            RestTemplate restTemplate, @Value("${app.clients.cart-crud.url}") String cartCrudUrl) {
        this.restTemplate = restTemplate;
        this.cartCrudUrl = cartCrudUrl;
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

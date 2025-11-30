package com.community.cart.cartpricing.infrastructure.client;

import com.community.cart.cartpricing.application.dto.CartSnapshotDTO;
import com.community.cart.cartpricing.application.dto.PricedCartItem;
import com.community.cart.cartpricing.config.CartPricingConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
public class CartSnapshotAdapter {
    private final RestTemplate restTemplate;
    private final String cartSnapshotBaseUrl;
    private final ObjectMapper objectMapper;

    // LLD: Inject the config class for URL
    public CartSnapshotAdapter(RestTemplate restTemplate, ObjectMapper objectMapper, CartPricingConfig config) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.cartSnapshotBaseUrl = config.getClients().getCartSnapshot().getUrl();
    }

    /**
     * Fetches a specific cart snapshot by its ID from the cart-snapshot service.
     */
    public CartSnapshotDTO getCartSnapshot(UUID snapshotId) {
        String url = cartSnapshotBaseUrl + "/api/v1/snapshots/" + snapshotId;
        try {
            return restTemplate.getForObject(url, CartSnapshotDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException("Cart Snapshot with ID " + snapshotId + " not found.", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to retrieve cart snapshot " + snapshotId + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Parses the JSON items string from the CartSnapshotDTO into a List of PricedCartItem.
     * This is a utility method for internal use.
     */
    public List<PricedCartItem> parseSnapshotItems(String itemsJson) {
        try {
            return objectMapper.readValue(itemsJson, new TypeReference<List<PricedCartItem>>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to parse cart items JSON from snapshot: " + ex.getMessage(), ex);
        }
    }
}

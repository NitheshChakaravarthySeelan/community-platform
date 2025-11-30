package com.community.cart.cartpricing.application.service;

import com.community.cart.cartpricing.application.dto.CartPricingRequest;
import com.community.cart.cartpricing.application.dto.CartPricingResponse;
import com.community.cart.cartpricing.application.dto.PricedCartItem;
import com.community.cart.cartpricing.infrastructure.client.CartSnapshotAdapter;
import com.community.cart.cartpricing.application.dto.CartSnapshotDTO;
import com.community.cart.cartpricing.CartPricingApplication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration; // Import to exclude
import org.springframework.boot.autoconfigure.EnableAutoConfiguration; // Annotation to use

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// LLD: @SpringBootTest loads a full application context. We need to exclude DataSource auto-configuration.
@SpringBootTest(classes = CartPricingApplication.class) // Specify the main application class
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class}) // Exclude data source auto-config
class CartPricingServiceTest {

    @Autowired
    private CartPricingService cartPricingService;

    @MockBean
    private CartSnapshotAdapter cartSnapshotAdapter;

    @Test
    void calculatePrice_shouldReturnCorrectTotals_forValidSnapshot() {
        // --- ARRANGE ---
        UUID snapshotId = UUID.randomUUID();
        CartPricingRequest pricingRequest = new CartPricingRequest(snapshotId);

        String itemsJson = "[{\"productId\":\"prod-123\",\"quantity\":2,\"name\":\"Test Product 1\",\"unitPriceCents\":1000,\"lineItemTotalCents\":2000}, {\"productId\":\"prod-456\",\"quantity\":1,\"name\":\"Test Product 2\",\"unitPriceCents\":550,\"lineItemTotalCents\":550}]";
        CartSnapshotDTO mockSnapshot = new CartSnapshotDTO(snapshotId, 123L, itemsJson, Instant.now());
        
        List<PricedCartItem> mockParsedItems = List.of(
            new PricedCartItem("prod-123", 2, "Test Product 1", 1000L, 2000L),
            new PricedCartItem("prod-456", 1, "Test Product 2", 550L, 550L)
        );

        // Define the behavior of our mock bean.
        when(cartSnapshotAdapter.getCartSnapshot(snapshotId)).thenReturn(mockSnapshot);
        when(cartSnapshotAdapter.parseSnapshotItems(itemsJson)).thenReturn(mockParsedItems);

        // --- ACT ---
        CartPricingResponse response = cartPricingService.calculatePrice(pricingRequest);

        // --- ASSERT ---
        assertNotNull(response);
        assertEquals(snapshotId, response.cartSnapshotId());
        
        // 2000 + 550 = 2550
        assertEquals(2550, response.subtotalCents());
        
        // For now, shipping, tax, and discount are 0.
        assertEquals(0, response.shippingCents());
        assertEquals(0, response.taxCents());
        assertEquals(0, response.discountCents());
        
        // Total should equal subtotal since other costs are zero.
        assertEquals(2550, response.totalCents());

        verify(cartSnapshotAdapter, times(1)).getCartSnapshot(snapshotId);
        verify(cartSnapshotAdapter, times(1)).parseSnapshotItems(itemsJson);
    }
    
    // Test for when snapshot is not found
    @Test
    void calculatePrice_shouldThrowException_whenSnapshotIsNotFound() {
        // --- ARRANGE ---
        UUID snapshotId = UUID.randomUUID();
        CartPricingRequest pricingRequest = new CartPricingRequest(snapshotId);
        
        when(cartSnapshotAdapter.getCartSnapshot(snapshotId))
            .thenThrow(new IllegalArgumentException("Cart Snapshot with ID " + snapshotId + " not found."));

        // --- ACT & ASSERT ---
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cartPricingService.calculatePrice(pricingRequest)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }
}

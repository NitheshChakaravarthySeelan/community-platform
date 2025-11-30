package com.community.cart.cartpricing.interfaces.controller;

import com.community.cart.cartpricing.application.dto.CartPricingRequest;
import com.community.cart.cartpricing.application.dto.CartPricingResponse;
import com.community.cart.cartpricing.application.service.CartPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor // Lombok generates constructor for final fields
public class CartPricingController {

    private final CartPricingService cartPricingService;

    @PostMapping("/calculate")
    public ResponseEntity<CartPricingResponse> calculateCartPrice(@RequestBody CartPricingRequest request) {
        // HLD: Pricing takes a snapshot ID, not the mutable cart.
        CartPricingResponse response = cartPricingService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }
}

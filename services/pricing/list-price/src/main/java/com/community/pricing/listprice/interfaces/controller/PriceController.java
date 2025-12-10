package com.community.pricing.listprice.interfaces.controller;

import com.community.pricing.listprice.application.service.PriceService;
import com.community.pricing.listprice.interfaces.dto.PriceRequest;
import com.community.pricing.listprice.interfaces.dto.PriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @PostMapping
    public ResponseEntity<PriceResponse> setPrice(@RequestBody PriceRequest request) {
        PriceResponse response = priceService.setPrice(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<PriceResponse> getPriceByProductId(@PathVariable UUID productId) {
        PriceResponse response = priceService.getPriceByProductId(productId);
        return ResponseEntity.ok(response);
    }
}

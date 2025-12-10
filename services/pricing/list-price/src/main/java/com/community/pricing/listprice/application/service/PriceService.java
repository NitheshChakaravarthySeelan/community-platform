package com.community.pricing.listprice.application.service;

import com.community.pricing.listprice.domain.model.Price;
import com.community.pricing.listprice.domain.repository.PriceRepository;
import com.community.pricing.listprice.interfaces.dto.PriceRequest;
import com.community.pricing.listprice.interfaces.dto.PriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceRepository priceRepository;

    @Transactional
    public PriceResponse setPrice(PriceRequest request) {
        Optional<Price> existingPrice = priceRepository.findByProductId(request.getProductId());

        Price price;
        if (existingPrice.isPresent()) {
            price = existingPrice.get();
            price.setPrice(request.getPrice());
            price.setCurrency(request.getCurrency());
            // lastUpdated is automatically set via @PreUpdate
        } else {
            price = Price.builder()
                    .productId(request.getProductId())
                    .price(request.getPrice())
                    .currency(request.getCurrency())
                    .lastUpdated(Instant.now())
                    .build();
        }

        Price savedPrice = priceRepository.save(price);
        return mapToResponse(savedPrice);
    }

    @Transactional(readOnly = true)
    public PriceResponse getPriceByProductId(UUID productId) {
        Price price = priceRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Price not found for product ID: " + productId));
        return mapToResponse(price);
    }

    private PriceResponse mapToResponse(Price price) {
        return new PriceResponse(
                price.getId(),
                price.getProductId(),
                price.getPrice(),
                price.getCurrency(),
                price.getLastUpdated()
        );
    }
}

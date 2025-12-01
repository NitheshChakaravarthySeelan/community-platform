package com.community.catalog.productread.application.handler;

import com.community.catalog.productread.application.command.GetProductByIdQuery; // Add this import
import com.community.catalog.productread.application.dto.ProductDTO;
import com.community.catalog.productread.domain.repository.ProductViewRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetProductByIdHandler {

  private final ProductViewRepository productViewRepository;

  @Cacheable(value = "products", key = "#query.productId")
  public Optional<ProductDTO> handle(GetProductByIdQuery query) {
    return productViewRepository.findById(query.getProductId())
        .map(product -> ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .quantity(product.getQuantity())
            .sku(product.getSku())
            .imageUrl(product.getImageUrl())
            .category(product.getCategory())
            .manufacturer(product.getManufacturer())
            .status(product.getStatus())
            .version(product.getVersion())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build());
  }
}

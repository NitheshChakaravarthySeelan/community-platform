package com.community.catalog.productread.application.handler;

import com.community.catalog.productread.application.dto.ProductDTO;
import com.community.catalog.productread.domain.model.ProductView;
import com.community.catalog.productread.domain.repository.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetAllProductsHandler {

    private final ProductViewRepository productViewRepository;

    public List<ProductDTO> handle(GetAllProductsQuery query) {
        List<ProductView> products = productViewRepository.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(ProductView product) {
        // This mapping logic can be moved to a dedicated mapper class later
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .category(product.getCategory())
                .manufacturer(product.getManufacturer())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .status(product.getStatus())
                .build();
    }
}

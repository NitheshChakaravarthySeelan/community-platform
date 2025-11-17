package com.community.catalog.brand.interfaces.dto;

import com.community.catalog.brand.domain.model.Brand;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BrandResponseDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BrandResponseDTO fromEntity(Brand brand) {
        BrandResponseDTO dto = new BrandResponseDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setDescription(brand.getDescription());
        dto.setCreatedAt(brand.getCreatedAt());
        dto.setUpdatedAt(brand.getUpdatedAt());
        return dto;
    }
}

package com.community.catalog.productwrite.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequestDTO {

    private String name;
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @PositiveOrZero
    private Integer stockQuantity;

    private String imageUrl;
    private String category;
    private String manufacturer;
    private String status;
}

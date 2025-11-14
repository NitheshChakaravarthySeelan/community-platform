package com.community.catalog.productwrite.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequestDTO {

    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 255)
    private String name;

    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0, message = "Stock quantity must be non-negative")
    private Integer stockQuantity;

    @NotBlank(message = "SKU cannot be empty")
    @Size(max = 100)
    private String sku;

    private String imageUrl;
    private String category;
    private String manufacturer;
    private String status;
}
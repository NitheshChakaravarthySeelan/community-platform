package com.community.catalog.productwrite.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class CreateProductCommand {
    // Product data
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final Integer stockQuantity;
    private final String sku;
    private final String imageUrl;
    private final String category;
    private final String manufacturer;
    private final String status;

    // User identity for authorization
    private final String userId;
    private final List<String> userRoles;
}

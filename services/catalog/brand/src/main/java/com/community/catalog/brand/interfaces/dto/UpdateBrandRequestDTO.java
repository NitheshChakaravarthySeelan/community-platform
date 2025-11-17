package com.community.catalog.brand.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBrandRequestDTO(@NotBlank String name, String description) {}

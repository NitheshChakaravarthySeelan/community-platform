package com.community.catalog.brand.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBrandRequestDTO(@NotBlank String name, String description) {}

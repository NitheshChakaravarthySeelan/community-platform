package com.community.catalog.category.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequestDTO(@NotBlank String name, String description, Long parentId) {}

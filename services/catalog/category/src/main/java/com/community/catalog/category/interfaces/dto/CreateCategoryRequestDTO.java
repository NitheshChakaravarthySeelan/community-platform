package com.community.catalog.category.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequestDTO(@NotBlank String name, String description, Long parentId) {}

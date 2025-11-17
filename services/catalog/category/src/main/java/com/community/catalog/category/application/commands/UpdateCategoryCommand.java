package com.community.catalog.category.application.commands;

public record UpdateCategoryCommand(Long id, String name, String description, Long parentId) {}

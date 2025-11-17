package com.community.catalog.category.application.commands;

public record CreateCategoryCommand(String name, String description, Long parentId) {}

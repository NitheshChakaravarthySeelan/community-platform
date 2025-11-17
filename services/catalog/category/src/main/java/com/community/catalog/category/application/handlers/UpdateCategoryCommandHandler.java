package com.community.catalog.category.application.handlers;

import com.community.catalog.category.application.commands.UpdateCategoryCommand;
import com.community.catalog.category.domain.model.Category;
import com.community.catalog.category.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateCategoryCommandHandler
        implements CommandHandler<Category, UpdateCategoryCommand> {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category handle(UpdateCategoryCommand command) {
        Category category =
                categoryRepository
                        .findById(command.id())
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Category not found with id: " + command.id()));

        Category parent = null;
        if (command.parentId() != null) {
            parent =
                    categoryRepository
                            .findById(command.parentId())
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "Parent category not found with id: "
                                                            + command.parentId()));
        }

        category.setName(command.name());
        category.setDescription(command.description());
        category.setParent(parent);
        return categoryRepository.save(category);
    }
}
